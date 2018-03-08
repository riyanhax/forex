package simulator;

import broker.OpenPositionRequest;
import broker.Quote;
import live.LiveTraders;
import live.Oanda;
import live.OandaTrader;
import market.ForexPortfolioValue;
import market.ForexPosition;
import market.ForexPositionValue;
import market.Instrument;
import market.InstrumentHistoryService;
import market.MarketEngine;
import market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import trader.ForexTrader;
import trader.TradingStrategy;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static broker.Quote.pipsFromPippetes;
import static market.MarketTime.formatRange;
import static market.MarketTime.formatTimestamp;

@Service
class HistoryDataForexBroker implements SimulatorForexBroker {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryDataForexBroker.class);

    private final MarketTime clock;
    private final MarketEngine marketEngine;
    private final InstrumentHistoryService instrumentHistoryService;

    private final Map<TradingStrategy, Collection<OandaTrader>> tradersByStrategy = new HashMap<>();
    private final List<OandaTrader> traders = new ArrayList<>();
    private final List<TradingStrategy> tradingStrategies;
    private final Map<String, TraderData> tradersByAccountNumber = new HashMap<>();

    private Simulation simulation;
    private SimulatorContext context;
    private Oanda broker;

    public HistoryDataForexBroker(MarketTime clock, MarketEngine marketEngine,
                                  InstrumentHistoryService instrumentHistoryService,
                                  List<TradingStrategy> tradingStrategies) {
        this.clock = clock;
        this.marketEngine = marketEngine;
        this.instrumentHistoryService = instrumentHistoryService;
        this.tradingStrategies = tradingStrategies;
    }

    @Override
    public void init(Simulation simulation) {
        this.simulation = simulation;

        this.tradersByStrategy.clear();
        this.traders.clear();
        this.tradersByAccountNumber.clear();
        this.context = new SimulatorContext(clock, instrumentHistoryService, marketEngine, simulation);

        marketEngine.init(simulation);

        tradingStrategies.forEach(it -> {
            try {
                tradersByStrategy.put(it, createInstances(it, simulation));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        this.traders.addAll(tradersByStrategy.values().stream().flatMap(Collection::stream).collect(Collectors.toList()));
        traders.forEach(trader -> tradersByAccountNumber.put(trader.getAccountNumber(), new TraderData()));

        this.broker = new Oanda(clock, instrumentHistoryService, new LiveTraders(traders));
    }

    private Collection<OandaTrader> createInstances(TradingStrategy tradingStrategy, Simulation simulation) throws Exception {
        List<OandaTrader> traders = new ArrayList<>();
        for (int i = 0; i < simulation.instancesPerTraderType; i++) {
            traders.add(new OandaTrader(tradingStrategy.toString() + "-" + i, context, tradingStrategy, clock, instrumentHistoryService));
        }
        return traders;
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
                getTraderData(it).addPortfolioValueSnapshot(getPortfolioValue(it));
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
            ForexPortfolioValue portfolioValue = getPortfolioValue(trader);
            Set<ForexPositionValue> positions = portfolioValue.getPositionValues();
            if (positions.isEmpty()) {
                // Not yet been filled
                return;
            }

            ForexPositionValue positionValue = positions.iterator().next();
            long pipsProfit = positionValue.pipettes();

            // Close once we've lost or gained enough pipettes or if it's noon Friday
            long stopLoss = openedPosition.getStopLoss().get();
            long takeProfit = openedPosition.getTakeProfit().get();

            if (pipsProfit < -stopLoss || pipsProfit > takeProfit) {
                closePosition(trader, positionValue.getPosition(), null);

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

        for (Map.Entry<TradingStrategy, Collection<OandaTrader>> e : tradersByStrategy.entrySet()) {
            TradingStrategy factory = e.getKey();
            Collection<OandaTrader> traders = e.getValue();

            LOG.info("\n\n{}:", factory.toString());

            long averageProfit = 0;

            SortedSet<ForexPortfolioValue> portfolios = new TreeSet<>(Comparator.comparing(ForexPortfolioValue::pipettes));
            SortedSet<ForexPositionValue> allTrades = new TreeSet<>();

            for (OandaTrader trader : traders) {
                ForexPortfolioValue end = getPortfolioValue(trader);
                long endPips = end.getPipettesProfit();
                LOG.info("End: {} at {}", profitLossDisplay(endPips), formatTimestamp(end.getTimestamp()));

                averageProfit += endPips;

                TraderData traderData = getTraderData(trader);
                portfolios.add(traderData.getDrawdownPortfolio());
                portfolios.add(traderData.getProfitPortfolio());
                portfolios.add(end);

                SortedSet<ForexPositionValue> closedTrades = context.closedTradesForAccountId(trader.getAccountNumber());
                allTrades.addAll(closedTrades);
            }

            averageProfit /= traders.size();

            SortedSet<ForexPositionValue> tradesSortedByProfit = new TreeSet<>(Comparator.comparing(ForexPositionValue::pipettes));
            tradesSortedByProfit.addAll(allTrades);
            ForexPositionValue worstTrade = tradesSortedByProfit.first();
            ForexPositionValue bestTrade = tradesSortedByProfit.last();

            ForexPortfolioValue drawdownPortfolio = portfolios.first();
            ForexPortfolioValue profitPortfolio = portfolios.last();

            LOG.info("Worst trade: {} from {}", profitLossDisplay(worstTrade.pipettes()), formatRange(worstTrade.getPosition().getOpened(), worstTrade.getTimestamp()));
            LOG.info("Best trade: {} from {}", profitLossDisplay(bestTrade.pipettes()), formatRange(bestTrade.getPosition().getOpened(), bestTrade.getTimestamp()));
            LOG.info("Profitable trades: {}/{}", allTrades.stream().filter(it -> it.pipettes() > 0).count(), allTrades.size());
            LOG.info("Highest drawdown: {} at {}", profitLossDisplay(drawdownPortfolio.pipettes()), formatTimestamp(drawdownPortfolio.getTimestamp()));
            LOG.info("Highest profit: {} at {}", profitLossDisplay(profitPortfolio.pipettes()), formatTimestamp(profitPortfolio.getTimestamp()));
            LOG.info("Average profit: {} from {}", profitLossDisplay(averageProfit), formatRange(simulation.startTime, simulation.endTime));
        }
    }

    private static String profitLossDisplay(long pipettes) {
        return String.format("%s pips, (%d pipettes)", pipsFromPippetes(pipettes), pipettes);
    }

    @Override
    public ForexPortfolioValue getPortfolioValue(ForexTrader trader) throws Exception {
        return broker.getPortfolioValue(trader);
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
    public void closePosition(ForexTrader trader, ForexPosition position, @Nullable Long limit) throws Exception {
        broker.closePosition(trader, position, limit);
    }

}
