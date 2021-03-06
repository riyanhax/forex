package forex.simulator;

import forex.broker.LiveTraders;
import forex.broker.Quote;
import forex.market.AccountSnapshot;
import forex.trader.ForexTrader;
import forex.trader.TradingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static forex.market.MarketTime.formatRange;
import static forex.market.MarketTime.formatTimestamp;
import static java.util.Comparator.comparing;

@Service
public class ResultsStdOutProcessor implements ResultsProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ResultsStdOutProcessor.class);

    @Override
    public void done(LiveTraders liveTraders, SimulatorContext context, SimulatorProperties simulatorProperties) {

        Map<TradingStrategy, List<ForexTrader>> tradersByStrategy = liveTraders.getTraders().stream()
                .collect(Collectors.groupingBy(ForexTrader::getStrategy));

        for (Map.Entry<TradingStrategy, List<ForexTrader>> e : tradersByStrategy.entrySet()) {
            TradingStrategy factory = e.getKey();
            Collection<ForexTrader> traders = e.getValue();

            LOG.info("\n\n{}:", factory.toString());

            long averageResult = 0;

            SortedSet<AccountSnapshot> portfolios = new TreeSet<>(comparing(AccountSnapshot::pipettes));
            SortedSet<TradeHistory> allTrades = new TreeSet<>(comparing(TradeHistory::getOpenTime));

            for (ForexTrader trader : traders) {
                AccountSnapshot end = context.getTraderData(trader.getAccountNumber()).getMostRecentPortfolio();

                LOG.info("End: {} at {}", profitLossDisplay(end), formatTimestamp(end.getTimestamp()));

                averageResult += end.getNetAssetValue();

                TraderData traderData = context.getTraderData(trader.getAccountNumber());
                portfolios.add(traderData.getDrawdownPortfolio());
                portfolios.add(traderData.getProfitPortfolio());
                portfolios.add(end);

                SortedSet<TradeHistory> closedTrades = context.closedTradesForAccountId(trader.getAccountNumber());
                allTrades.addAll(closedTrades);
            }

            averageResult /= traders.size();

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
            LOG.info("Average profit: {} from {}", Quote.profitLossDisplay(averageResult), formatRange(simulatorProperties.getStartTime(), simulatorProperties.getEndTime()));
        }
    }

    private static String profitLossDisplay(AccountSnapshot portfolio) {
        return Quote.profitLossDisplay(portfolio.getNetAssetValue());
    }

    private static String profitLossDisplay(TradeHistory trade) {
        return Quote.profitLossDisplay(trade.getRealizedProfitLoss()) + ", " + trade.getInitialUnits() + " units";
    }
}


