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
import trader.ForexTraderFactory;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static market.MarketTime.formatRange;
import static market.MarketTime.formatTimestamp;

@Service
class HistoryDataForexBroker implements SimulatorForexBroker {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryDataForexBroker.class);

    private final MarketTime clock;
    private final MarketEngine marketEngine;
    private final Map<String, ForexTrader> tradersByOrderId = new HashMap<>();
    private final Map<ForexTraderFactory, Collection<ForexTrader>> tradersByFactory = new IdentityHashMap<>();
    private final List<ForexTraderFactory> traderFactories;
    private List<ForexTrader> traders;

    private Simulation simulation;

    public HistoryDataForexBroker(MarketTime clock, MarketEngine marketEngine,
                                  List<ForexTraderFactory> traderFactories) {
        this.clock = clock;
        this.marketEngine = marketEngine;
        this.traderFactories = traderFactories;
    }

    @Override
    public void init(Simulation simulation) {
        this.simulation = simulation;

        this.tradersByOrderId.clear();
        this.tradersByFactory.clear();

        marketEngine.init(simulation);

        traderFactories.forEach(it -> tradersByFactory.put(it, it.createInstances(simulation)));
        this.traders = tradersByFactory.entrySet().stream()
                .map(Map.Entry::getValue).flatMap(Collection::stream)
                .collect(Collectors.toList());

        traders.forEach(it -> it.init(simulation));
    }

    @Override
    public void processUpdates() throws Exception {

        if (isClosed()) {
            return;
        }

        // Update prices and process any limit/stop orders
        marketEngine.processUpdates();

        for (ForexTrader trader : traders) {
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
    private void handleStopLossTakeProfits(ForexTrader trader) {
        OpenPositionRequest openedPosition = trader.getOpenedPosition();

        if (openedPosition != null) {
            ForexPortfolioValue portfolioValue = getPortfolioValue(trader);
            Set<ForexPositionValue> positions = portfolioValue.getPositionValues();
            if (positions.isEmpty()) {
                // Not yet been filled
                return;
            }

            ForexPositionValue positionValue = positions.iterator().next();
            double pipsProfit = positionValue.pips();

            // Close once we've lost or gained enough pips or if it's noon Friday
            double stopLoss = openedPosition.getStopLoss().get();
            double takeProfit = openedPosition.getTakeProfit().get();

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

        tradersByFactory.forEach((factory, traders) -> {

            LOG.info("\n\n{}:", factory.getClass().getName());

            double averageProfit = 0d;

            SortedSet<ForexPortfolioValue> portfolios = new TreeSet<>(Comparator.comparing(ForexPortfolioValue::pips));
            SortedSet<ForexPositionValue> tradesSortedByProfit = new TreeSet<>(Comparator.comparing(ForexPositionValue::pips));

            for (ForexTrader trader : traders) {
                ForexPortfolioValue end = trader.getMostRecentPortfolio();
                double endPips = end.pips();
                LOG.info("End: {} pips at {}", endPips, formatTimestamp(end.getTimestamp()));

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

            LOG.info("Worst trade: {} pips from {}", worstTrade.pips(), formatRange(worstTrade.getPosition().getOpened(), worstTrade.getTimestamp()));
            LOG.info("Best trade: {} pips from {}", bestTrade.pips(), formatRange(bestTrade.getPosition().getOpened(), bestTrade.getTimestamp()));
            LOG.info("Profitable trades: {}/{}", tradesSortedByProfit.stream().filter(it -> it.pips() > 0).count(), tradesSortedByProfit.size());
            LOG.info("Highest drawdown: {} pips at {}", drawdownPortfolio.pips(), formatTimestamp(drawdownPortfolio.getTimestamp()));
            LOG.info("Highest profit: {} pips at {}", profitPortfolio.pips(), formatTimestamp(profitPortfolio.getTimestamp()));
            LOG.info("Average profit: {} pips from {}", averageProfit, formatRange(simulation.startTime, simulation.endTime));
        });
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
            quote = getQuote(position.getInstrument());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ForexPositionValue(position, clock.now(), position.getStance() == Stance.LONG ? quote.getBid() : quote.getAsk());
    }

    @Override
    public Quote getQuote(Instrument pair) throws Exception {
        double price = marketEngine.getPrice(pair);
        double halfSpread = halfSpread(pair);

        return new BidAsk(price - halfSpread, price + halfSpread);
    }

    private double halfSpread(Instrument pair) {
        return (simulation.pipSpread * pair.getPip()) / 2;
    }

    @Override
    public void orderFilled(OrderRequest filled) {
        Instrument instrument = filled.getInstrument();
        double commission = (filled.isBuyOrder() ? -1 : 1) * halfSpread(instrument);
        double price = filled.getExecutionPrice().get() + commission;

        ForexTrader trader = tradersByOrderId.get(filled.getId());
        ForexPortfolio oldPortfolio = trader.getPortfolio();
        ImmutableMap<Instrument, ForexPosition> positionsByInstrument = Maps.uniqueIndex(oldPortfolio.getPositions(), ForexPosition::getInstrument);
        Map<Instrument, ForexPosition> newPositions = new HashMap<>(positionsByInstrument);
        ForexPosition existingPosition = positionsByInstrument.get(instrument);
        double newPipsProfit = oldPortfolio.getPipsProfit();
        SortedSet<ForexPositionValue> closedTrades = new TreeSet<>(oldPortfolio.getClosedTrades());

        if (filled.isSellOrder()) {
            Objects.requireNonNull(existingPosition, "Go long on the inverse pair, instead of shorting the primary pair.");

            ForexPositionValue closedTrade = positionValue(existingPosition);
            newPipsProfit += closedTrade.pips();

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
    public OrderRequest getOrder(OrderRequest order) {
        return marketEngine.getOrder(order);
    }

    @Override
    public void openPosition(ForexTrader trader, OpenPositionRequest request) {

        Set<ForexPosition> positions = trader.getPortfolio().getPositions();
        Preconditions.checkArgument(positions.isEmpty(), "Currently only one open position is allowed at a time");

        // Open a long position on USD/EUR to simulate a short position for EUR/USD
        Instrument pair = request.getPair();
        Optional<Double> limit = request.getLimit();

        OrderRequest submitted;
        if (limit.isPresent()) {
            BuyLimitOrder order = Orders.buyLimitOrder(1, pair, limit.get());
            submitted = marketEngine.submit(this, order);
        } else {
            BuyMarketOrder order = Orders.buyMarketOrder(1, pair);
            submitted = marketEngine.submit(this, order);
        }
        orderSubmitted(trader, submitted);
        trader.setOpenedPosition(request);
    }

    @Override
    public void closePosition(ForexTrader trader, ForexPosition position, @Nullable Double limit) {
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
