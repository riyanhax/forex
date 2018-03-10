package simulator;

import broker.CandlestickData;
import broker.ForexBroker;
import broker.Quote;
import com.google.common.io.Files;
import live.LiveTraders;
import live.OandaTrader;
import market.AccountSnapshot;
import market.BaseWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trader.ForexTrader;
import trader.TradingStrategy;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static broker.Quote.pipsFromPippetes;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static market.MarketTime.formatRange;
import static market.MarketTime.formatTimestamp;

@Service
class Simulator extends BaseWatcher<SimulatorClock, ForexBroker> {

    private static final Logger LOG = LoggerFactory.getLogger(Simulator.class);
    private final SimulatorProperties simulatorProperties;
    private final SimulatorContext context;
    private final LiveTraders traders;

    @Autowired
    public Simulator(SimulatorProperties simulatorProperties,
                     SimulatorClock clock,
                     ForexBroker broker,
                     SimulatorContext context,
                     LiveTraders traders) {
        super(clock, broker);

        this.simulatorProperties = simulatorProperties;
        this.context = context;
        this.traders = traders;
    }

    @Override
    public void run() throws Exception {
        super.run();

        done();
    }

    @Override
    public boolean keepGoing(LocalDateTime now) {
        return now.isBefore(simulatorProperties.getEndTime());
    }

    @Override
    public long millisUntilNextInterval() {
        return simulatorProperties.getMillisDelayBetweenMinutes();
    }

    @Override
    protected void nextMinute() throws Exception {
        LocalDateTime previous = clock.now();
        if (!previous.isBefore(simulatorProperties.getEndTime())) {
            throw new IllegalStateException("Can't advance beyond the end of the simulation!");
        }

        clock.advance(1, MINUTES);

        if (!context.isAvailable()) {
            return;
        }

        context.beforeTraders();

        super.nextMinute();

        context.afterTraders();
    }

    @Override
    public boolean logTime(LocalDateTime now) {
        return now.getMinute() == 0 && now.getHour() == 0 && now.getSecond() == 0;
    }

    private void done() throws IOException {

        Map<TradingStrategy, List<OandaTrader>> tradersByStrategy = traders.getTraders().stream()
                .collect(Collectors.groupingBy(ForexTrader::getStrategy));

        for (Map.Entry<TradingStrategy, List<OandaTrader>> e : tradersByStrategy.entrySet()) {
            TradingStrategy factory = e.getKey();
            Collection<OandaTrader> traders = e.getValue();

            LOG.info("\n\n{}:", factory.toString());

            long averageProfit = 0;

            SortedSet<AccountSnapshot> portfolios = new TreeSet<>(comparing(AccountSnapshot::pipettes));
            SortedSet<TradeHistory> allTrades = new TreeSet<>();

            for (OandaTrader trader : traders) {
                AccountSnapshot end = context.getTraderData(trader.getAccountNumber()).getMostRecentPortfolio();
                long endPips = end.getPipettesProfit();
                LOG.info("End: {} at {}", profitLossDisplay(endPips), formatTimestamp(end.getTimestamp()));

                averageProfit += endPips;

                TraderData traderData = context.getTraderData(trader.getAccountNumber());
                portfolios.add(traderData.getDrawdownPortfolio());
                portfolios.add(traderData.getProfitPortfolio());
                portfolios.add(end);

                SortedSet<TradeHistory> closedTrades = context.closedTradesForAccountId(trader.getAccountNumber());
                allTrades.addAll(closedTrades);

                StringBuilder sb = new StringBuilder();
                long portfolioPips = 0L;
                sb.append(formatTimestamp(simulatorProperties.getStartTime()))
                        .append(",")
                        .append(portfolioPips);

                for (TradeHistory trade : closedTrades) {
                    portfolioPips += trade.getRealizedProfitLoss();

                    sb.append("\n");
                    sb.append(formatTimestamp(trade.getCloseTime()))
                            .append(",")
                            .append(portfolioPips);
                }

                File file = new File("build/" + trader.getAccountNumber() + "-portfolio.csv");
                if (!file.exists()) {
                    file.createNewFile();
                }

                Files.write(sb.toString().getBytes(), file);

                sb = new StringBuilder();
                for (TradeHistory trade : closedTrades) {
                    List<String> columns = new ArrayList<>();
                    columns.add(trade.getId());
                    columns.add(trade.getInstrument().getSymbol());
                    columns.add(Integer.toString(trade.getCurrentUnits()));
                    columns.addAll(trade.getCandles().values().stream()
                            .map(CandlestickData::getO)
                            .map(Quote::doubleFromPippetes)
                            .map(it -> Double.toString(it))
                            .collect(toList()));

                    sb.append(columns.stream().collect(joining(",")))
                            .append("\n");
                }

                file = new File("build/" + trader.getAccountNumber() + "-trades.csv");
                if (!file.exists()) {
                    file.createNewFile();
                }

                Files.write(sb.toString().getBytes(), file);
            }

            averageProfit /= traders.size();

            SortedSet<TradeHistory> tradesSortedByProfit = new TreeSet<>(comparing(TradeHistory::getRealizedProfitLoss));
            tradesSortedByProfit.addAll(allTrades);
            TradeHistory worstTrade = tradesSortedByProfit.first();
            TradeHistory bestTrade = tradesSortedByProfit.last();

            AccountSnapshot drawdownPortfolio = portfolios.first();
            AccountSnapshot profitPortfolio = portfolios.last();

            LOG.info("Worst trade: {} from {}", profitLossDisplay(worstTrade), formatRange(worstTrade.getOpenTime(), worstTrade.getCloseTime()));
            LOG.info("Best trade: {} from {}", profitLossDisplay(bestTrade), formatRange(bestTrade.getOpenTime(), bestTrade.getCloseTime()));
            LOG.info("Profitable trades: {}/{}", allTrades.stream().filter(it -> it.getRealizedProfitLoss() > 0).count(), allTrades.size());
            LOG.info("Highest drawdown: {} at {}", profitLossDisplay(drawdownPortfolio), formatTimestamp(drawdownPortfolio.getTimestamp()));
            LOG.info("Highest profit: {} at {}", profitLossDisplay(profitPortfolio), formatTimestamp(profitPortfolio.getTimestamp()));
            LOG.info("Average profit: {} from {}", profitLossDisplay(averageProfit), formatRange(simulatorProperties.getStartTime(), simulatorProperties.getEndTime()));
        }
    }

    private static String profitLossDisplay(AccountSnapshot portfolio) {
        return profitLossDisplay(portfolio.pipettes());
    }

    private static String profitLossDisplay(TradeHistory trade) {
        return profitLossDisplay(trade.getRealizedProfitLoss());
    }

    private static String profitLossDisplay(long pipettes) {
        return String.format("%s pips, (%d pipettes)", pipsFromPippetes(pipettes), pipettes);
    }
}
