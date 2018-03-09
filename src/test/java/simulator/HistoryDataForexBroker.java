package simulator;

import broker.CandlestickData;
import broker.ForexBroker;
import broker.OpenPositionRequest;
import broker.Quote;
import broker.RequestException;
import broker.TradeSummary;
import com.google.common.collect.Range;
import live.LiveTraders;
import live.Oanda;
import live.OandaTrader;
import market.AccountSnapshot;
import market.Instrument;
import market.MarketEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import trader.ForexTrader;
import trader.TradingStrategy;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static broker.Quote.pipsFromPippetes;
import static java.util.Comparator.comparing;
import static market.MarketTime.formatRange;
import static market.MarketTime.formatTimestamp;

@Service
class HistoryDataForexBroker implements SimulatorForexBroker {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryDataForexBroker.class);

    private final MarketEngine marketEngine;

    private final List<OandaTrader> traders = new ArrayList<>();
    private final Map<String, TraderData> tradersByAccountNumber = new HashMap<>();

    private Simulation simulation;
    private SimulatorContext context;
    private ForexBroker broker;

    public HistoryDataForexBroker(Oanda broker, MarketEngine marketEngine,
                                  LiveTraders traders, SimulatorContext context) {
        this.broker = broker;
        this.marketEngine = marketEngine;
        this.traders.addAll(traders.getTraders());
        this.context = context;
    }

    @Override
    public void init(Simulation simulation) {
        this.simulation = simulation;

        traders.forEach(trader -> tradersByAccountNumber.put(trader.getAccountNumber(), new TraderData()));
    }


    @Override
    public void processUpdates() throws Exception {

        if (isClosed()) {
            return;
        }

        // Update prices and process any limit/stop orders
        marketEngine.processUpdates();

        for (OandaTrader trader : traders) {
            // TODO: The market needs to manage stop loss/take profit orders
            handleStopLossTakeProfits(trader);

            // Allow traders to make/close positions
            trader.processUpdates(this);
        }

        // Process any submitted orders
        marketEngine.processUpdates();

        // Update portfolio snapshots
        traders.forEach(it -> {
            try {
                getTraderData(it).addSnapshot(getAccountSnapshot(it));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /*
     * All of this logic should be moved to be handled with orders in the market.
     * @param trader
     */
    private void handleStopLossTakeProfits(OandaTrader trader) throws Exception {
        OpenPositionRequest openedPosition = getTraderData(trader).getOpenedPosition();

        if (openedPosition != null) {
            AccountSnapshot accountSnapshot = getAccountSnapshot(trader);
            List<TradeSummary> positions = accountSnapshot.getPositionValues();
            if (positions.isEmpty()) {
                // Not yet been filled
                return;
            }

            TradeSummary positionValue = positions.iterator().next();
            long pipsProfit = positionValue.getUnrealizedPL();

            // Close once we've lost or gained enough pipettes or if it's noon Friday
            long stopLoss = openedPosition.getStopLoss().get();
            long takeProfit = openedPosition.getTakeProfit().get();

            if (pipsProfit < -stopLoss || pipsProfit > takeProfit) {
                closePosition(trader, positionValue, null);

                // Skipping trader because this was their theoretical action in the old format
                // This may not be necessary in reality
                getTraderData(trader).setOpenedPosition(null);
            }
        }
    }

    private TraderData getTraderData(ForexTrader trader) {
        return tradersByAccountNumber.get(trader.getAccountNumber());
    }

    @Override
    public void done() throws Exception {

        Map<TradingStrategy, List<OandaTrader>> tradersByStrategy = traders.stream()
                .collect(Collectors.groupingBy(ForexTrader::getStrategy));

        for (Map.Entry<TradingStrategy, List<OandaTrader>> e : tradersByStrategy.entrySet()) {
            TradingStrategy factory = e.getKey();
            Collection<OandaTrader> traders = e.getValue();

            LOG.info("\n\n{}:", factory.toString());

            long averageProfit = 0;

            SortedSet<AccountSnapshot> portfolios = new TreeSet<>(comparing(AccountSnapshot::pipettes));
            SortedSet<TradeSummary> allTrades = new TreeSet<>();

            for (OandaTrader trader : traders) {
                AccountSnapshot end = getAccountSnapshot(trader);
                long endPips = end.getPipettesProfit();
                LOG.info("End: {} at {}", profitLossDisplay(endPips), formatTimestamp(end.getTimestamp()));

                averageProfit += endPips;

                TraderData traderData = getTraderData(trader);
                portfolios.add(traderData.getDrawdownPortfolio());
                portfolios.add(traderData.getProfitPortfolio());
                portfolios.add(end);

                SortedSet<TradeSummary> closedTrades = context.closedTradesForAccountId(trader.getAccountNumber());
                allTrades.addAll(closedTrades);
            }

            averageProfit /= traders.size();

            SortedSet<TradeSummary> tradesSortedByProfit = new TreeSet<>(comparing(TradeSummary::getRealizedProfitLoss));
            tradesSortedByProfit.addAll(allTrades);
            TradeSummary worstTrade = tradesSortedByProfit.first();
            TradeSummary bestTrade = tradesSortedByProfit.last();

            AccountSnapshot drawdownPortfolio = portfolios.first();
            AccountSnapshot profitPortfolio = portfolios.last();

            LOG.info("Worst trade: {} from {}", profitLossDisplay(worstTrade), formatRange(worstTrade.getOpenTime(), worstTrade.getCloseTime()));
            LOG.info("Best trade: {} from {}", profitLossDisplay(bestTrade), formatRange(bestTrade.getOpenTime(), bestTrade.getCloseTime()));
            LOG.info("Profitable trades: {}/{}", allTrades.stream().filter(it -> it.getRealizedProfitLoss() > 0).count(), allTrades.size());
            LOG.info("Highest drawdown: {} at {}", profitLossDisplay(drawdownPortfolio), formatTimestamp(drawdownPortfolio.getTimestamp()));
            LOG.info("Highest profit: {} at {}", profitLossDisplay(profitPortfolio), formatTimestamp(profitPortfolio.getTimestamp()));
            LOG.info("Average profit: {} from {}", profitLossDisplay(averageProfit), formatRange(simulation.getStartTime(), simulation.getEndTime()));
        }
    }

    private static String profitLossDisplay(AccountSnapshot portfolio) {
        return profitLossDisplay(portfolio.pipettes());
    }

    private static String profitLossDisplay(TradeSummary trade) {
        return profitLossDisplay(trade.getRealizedProfitLoss());
    }

    private static String profitLossDisplay(long pipettes) {
        return String.format("%s pips, (%d pipettes)", pipsFromPippetes(pipettes), pipettes);
    }

    @Override
    public AccountSnapshot getAccountSnapshot(ForexTrader trader) throws Exception {
        return broker.getAccountSnapshot(trader);
    }

    @Override
    public Quote getQuote(ForexTrader trader, Instrument pair) throws Exception {
        return broker.getQuote(trader, pair);
    }

    @Override
    public boolean isClosed() {
        return broker.isClosed() || !marketEngine.isAvailable();
    }

    @Override
    public boolean isClosed(LocalDate time) {
        return broker.isClosed(time) || !marketEngine.isAvailable(time);
    }

    @Override
    public void openPosition(ForexTrader trader, OpenPositionRequest request) throws Exception {
        broker.openPosition(trader, request);
        getSimulatorTrader(trader).setOpenedPosition(request);
    }

    private TraderData getSimulatorTrader(ForexTrader trader) {
        return getTraderData(trader);
    }

    @Override
    public void closePosition(ForexTrader trader, TradeSummary position, @Nullable Long limit) throws Exception {
        broker.closePosition(trader, position, limit);
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getOneDayCandles(ForexTrader trader, Instrument pair, Range<LocalDateTime> closed) throws RequestException {
        return broker.getOneDayCandles(trader, pair, closed);
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getFourHourCandles(ForexTrader trader, Instrument pair, Range<LocalDateTime> closed) throws RequestException {
        return broker.getFourHourCandles(trader, pair, closed);
    }

}
