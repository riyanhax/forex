package simulator;

import broker.BidAsk;
import broker.OpenPositionRequest;
import broker.Quote;
import broker.Stance;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import market.ForexPortfolio;
import market.ForexPortfolioValue;
import market.ForexPosition;
import market.ForexPositionValue;
import market.Instrument;
import market.InstrumentHistoryService;
import market.MarketEngine;
import market.MarketTime;
import market.order.BuyLimitOrder;
import market.order.BuyMarketOrder;
import market.order.OrderRequest;
import market.order.Orders;
import market.order.SellLimitOrder;
import market.order.SellMarketOrder;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static broker.Quote.pipsFromPippetes;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static market.MarketTime.formatRange;
import static market.MarketTime.formatTimestamp;

@Service
class HistoryDataForexBroker implements SimulatorForexBroker {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryDataForexBroker.class);

    private final MarketTime clock;
    private final MarketEngine marketEngine;
    private final InstrumentHistoryService instrumentHistoryService;

    private final Map<String, ForexTrader> tradersByOrderId = new HashMap<>();
    private final Map<TradingStrategy, Collection<SimulatorForexTrader>> tradersByStrategy = new HashMap<>();
    private final List<TradingStrategy> tradingStrategies;
    private final Map<String, SimulatorForexTrader> tradersByAccountNumber = new HashMap<>();

    private Simulation simulation;
    private SimulatorContext context;

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

        this.tradersByOrderId.clear();
        this.tradersByStrategy.clear();
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
        this.tradersByAccountNumber.putAll(tradersByStrategy.entrySet().stream()
                .map(Map.Entry::getValue).flatMap(Collection::stream)
                .collect(toMap(ForexTrader::getAccountNumber, identity())));
    }

    private Collection<SimulatorForexTrader> createInstances(TradingStrategy tradingStrategy, Simulation simulation) throws Exception {
        List<SimulatorForexTrader> traders = new ArrayList<>();
        for (int i = 0; i < simulation.instancesPerTraderType; i++) {
            traders.add(new SimulatorForexTrader(tradingStrategy.toString() + "-" + i, context, tradingStrategy, clock, instrumentHistoryService));
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

        Collection<SimulatorForexTrader> traders = this.tradersByAccountNumber.values();
        for (SimulatorForexTrader trader : traders) {
            // TODO: The market needs to manage stop loss/take profit orders
            handleStopLossTakeProfits(trader);

            // Allow traders to make/close positions
            trader.processUpdates(this);
        }

        // Process any submitted orders
        marketEngine.processUpdates();

        // Update portfolio snapshots
        traders.forEach(it -> it.addPortfolioValueSnapshot(getPortfolioValue(it)));
    }

    /*
     * All of this logic should be moved to be handled with orders in the market.
     * @param trader
     */
    private void handleStopLossTakeProfits(SimulatorForexTrader trader) {
        OpenPositionRequest openedPosition = trader.getOpenedPosition();

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
                trader.setOpenedPosition(null);
            }
        }
    }

    @Override
    public void done() {

        tradersByStrategy.forEach((factory, traders) -> {

            LOG.info("\n\n{}:", factory.toString());

            long averageProfit = 0;

            SortedSet<ForexPortfolioValue> portfolios = new TreeSet<>(Comparator.comparing(ForexPortfolioValue::pipettes));
            SortedSet<ForexPositionValue> tradesSortedByProfit = new TreeSet<>(Comparator.comparing(ForexPositionValue::pipettes));

            for (SimulatorForexTrader trader : traders) {
                ForexPortfolioValue end = trader.getMostRecentPortfolio();
                long endPips = end.pipettes();
                LOG.info("End: {} pipettes at {}", endPips, formatTimestamp(end.getTimestamp()));

                averageProfit += endPips;

                portfolios.add(trader.getDrawdownPortfolio());
                portfolios.add(trader.getProfitPortfolio());

                ForexPortfolio portfolio = end.getPortfolio();
                tradesSortedByProfit.addAll(portfolio.getClosedTrades());
            }

            averageProfit /= traders.size();

            ForexPositionValue worstTrade = tradesSortedByProfit.first();
            ForexPositionValue bestTrade = tradesSortedByProfit.last();

            ForexPortfolioValue drawdownPortfolio = portfolios.first();
            ForexPortfolioValue profitPortfolio = portfolios.last();

            LOG.info("Worst trade: {} from {}", profitLossDisplay(worstTrade.pipettes()), formatRange(worstTrade.getPosition().getOpened(), worstTrade.getTimestamp()));
            LOG.info("Best trade: {} from {}", profitLossDisplay(bestTrade.pipettes()), formatRange(bestTrade.getPosition().getOpened(), bestTrade.getTimestamp()));
            LOG.info("Profitable trades: {}/{}", tradesSortedByProfit.stream().filter(it -> it.pipettes() > 0).count(), tradesSortedByProfit.size());
            LOG.info("Highest drawdown: {} at {}", profitLossDisplay(drawdownPortfolio.pipettes()), formatTimestamp(drawdownPortfolio.getTimestamp()));
            LOG.info("Highest profit: {} at {}", profitLossDisplay(profitPortfolio.pipettes()), formatTimestamp(profitPortfolio.getTimestamp()));
            LOG.info("Average profit: {} from {}", profitLossDisplay(averageProfit), formatRange(simulation.startTime, simulation.endTime));
        });
    }

    private static String profitLossDisplay(long pipettes) {
        return String.format("%s pips, (%d pipettes)", pipsFromPippetes(pipettes), pipettes);
    }

    @Override
    public ForexPortfolioValue getPortfolioValue(ForexTrader trader) {
        ForexPortfolio portfolio = trader.getPortfolio();
        return portfolioValue(portfolio);
    }

    private ForexPortfolioValue portfolioValue(ForexPortfolio portfolio) {
        Set<ForexPosition> positions = portfolio.getPositions();
        Set<ForexPositionValue> positionValues = positionValues(positions);

        return new ForexPortfolioValue(portfolio, clock.now(), positionValues);
    }

    private Set<ForexPositionValue> positionValues(Set<ForexPosition> positions) {
        return positions.stream()
                .map(this::positionValue)
                .collect(Collectors.toSet());
    }

    private ForexPositionValue positionValue(ForexPosition position) {
        Quote quote = null;
        try {
            quote = getQuote(null, position.getInstrument());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ForexPositionValue(position, clock.now(), position.getStance() == Stance.LONG ? quote.getBid() : quote.getAsk());
    }

    @Override
    public Quote getQuote(ForexTrader trader, Instrument pair) throws Exception {
        long price = marketEngine.getPrice(pair);
        long halfSpread = halfSpread(pair);

        return new BidAsk(price - halfSpread, price + halfSpread);
    }

    private long halfSpread(Instrument pair) {
        return (simulation.pippeteSpread / 2);
    }

    @Override
    public void orderFilled(OrderRequest filled) {
        Instrument instrument = filled.getInstrument();
        long commission = (filled.isBuyOrder() ? -1 : 1) * halfSpread(instrument);
        long price = filled.getExecutionPrice().get() + commission;

        ForexTrader trader = tradersByOrderId.get(filled.getId());
        ForexPortfolio oldPortfolio = trader.getPortfolio();
        ImmutableMap<Instrument, ForexPosition> positionsByInstrument = Maps.uniqueIndex(oldPortfolio.getPositions(), ForexPosition::getInstrument);
        Map<Instrument, ForexPosition> newPositions = new HashMap<>(positionsByInstrument);
        ForexPosition existingPosition = positionsByInstrument.get(instrument);
        long newPipsProfit = oldPortfolio.getPipettesProfit();
        SortedSet<ForexPositionValue> closedTrades = new TreeSet<>(oldPortfolio.getClosedTrades());

        if (filled.isSellOrder()) {
            Objects.requireNonNull(existingPosition, "Go long on the inverse pair, instead of shorting the primary pair.");

            ForexPositionValue closedTrade = positionValue(existingPosition);
            newPipsProfit += closedTrade.pipettes();

            newPositions.remove(instrument);
            closedTrades.add(closedTrade);
        } else if (filled.isBuyOrder()) {
            Preconditions.checkArgument(existingPosition == null, "Shouldn't have more than one position open for a pair at a time!");
            Preconditions.checkArgument(!positionsByInstrument.containsKey(instrument.getOpposite()), "Shouldn't have more than one position open for a pair at a time!");

            newPositions.put(instrument, new ForexPosition(clock.now(), instrument, Stance.LONG, price));
        }

        ForexPortfolio portfolio = new ForexPortfolio(newPipsProfit, new HashSet<>(newPositions.values()), closedTrades);

        trader.setPortfolio(portfolio);
    }

    @Override
    public void orderCancelled(OrderRequest cancelled) {
        ForexTrader trader = tradersByOrderId.get(cancelled.getId());
        trader.cancelled(cancelled);
    }

    @Override
    public boolean isClosed() {
        return !marketEngine.isAvailable();
    }

    @Override
    public boolean isClosed(LocalDate time) {
        return !marketEngine.isAvailable(time);
    }

    @Override
    public void openPosition(ForexTrader trader, OpenPositionRequest request) {

        Set<ForexPosition> positions = trader.getPortfolio().getPositions();
        Preconditions.checkArgument(positions.isEmpty(), "Currently only one open position is allowed at a time");

        // Open a long position on USD/EUR to simulate a short position for EUR/USD
        Instrument pair = request.getPair();
        Optional<Long> limit = request.getLimit();

        OrderRequest submitted;
        if (limit.isPresent()) {
            BuyLimitOrder order = Orders.buyLimitOrder(1, pair, limit.get());
            submitted = marketEngine.submit(this, order);
        } else {
            BuyMarketOrder order = Orders.buyMarketOrder(1, pair);
            submitted = marketEngine.submit(this, order);
        }
        orderSubmitted(trader, submitted);
        getSimulatorTrader(trader).setOpenedPosition(request);
    }

    private SimulatorForexTrader getSimulatorTrader(ForexTrader trader) {
        return this.tradersByAccountNumber.get(trader.getAccountNumber());
    }

    @Override
    public void closePosition(ForexTrader trader, ForexPosition position, @Nullable Long limit) {
        OrderRequest submitted;
        if (limit == null) {
            SellMarketOrder order = Orders.sellMarketOrder(1, position.getInstrument());
            submitted = marketEngine.submit(this, order);
        } else {
            SellLimitOrder order = Orders.sellLimitOrder(1, position.getInstrument(), limit);
            submitted = marketEngine.submit(this, order);
        }
        orderSubmitted(trader, submitted);
    }

    private OrderRequest orderSubmitted(ForexTrader trader, OrderRequest submitted) {
        tradersByOrderId.put(submitted.getId(), trader);
        return submitted;
    }
}
