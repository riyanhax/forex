package simulator;

import broker.BidAsk;
import broker.Quote;
import broker.Stance;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import market.MarketEngine;
import market.MarketTime;
import market.forex.ForexPortfolio;
import market.forex.ForexPortfolioValue;
import market.forex.ForexPosition;
import market.forex.ForexPositionValue;
import market.forex.Instrument;
import market.order.BuyLimitOrder;
import market.order.BuyMarketOrder;
import market.order.OrderRequest;
import market.order.Orders;
import market.order.SellLimitOrder;
import market.order.SellMarketOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import trader.forex.ForexTrader;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
class HistoryDataForexBroker implements SimulatorForexBroker {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryDataForexBroker.class);

    private final MarketTime clock;
    private final MarketEngine marketEngine;
    private final Map<String, ForexTrader> tradersByOrderId = new HashMap<>();
    private final List<ForexTrader> traders = new ArrayList<>();

    private Simulation simulation;

    public HistoryDataForexBroker(MarketTime clock, MarketEngine marketEngine) {
        this.clock = clock;
        this.marketEngine = marketEngine;
    }

    @Override
    public void init(Simulation simulation, Collection<ForexTrader> traders) {
        this.simulation = simulation;

        this.tradersByOrderId.clear();
        this.traders.clear();
        this.traders.addAll(traders);

        marketEngine.init(simulation);

        traders.forEach(it -> it.init(simulation));
    }

    @Override
    public void processUpdates() {

        if (!isOpen()) {
            return;
        }

        // Update prices and process any limit/stop orders
        marketEngine.processUpdates();

        // Allow traders to make/close positions
        traders.forEach(it -> it.processUpdates(this));

        // Process any submitted orders
        marketEngine.processUpdates();

        // Update portfolio snapshots
        traders.forEach(it -> it.addPortfolioValueSnapshot(getPortfolioValue(it)));
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
        Quote quote = getQuote(position.getInstrument());
        return new ForexPositionValue(position, clock.now(), position.getStance() == Stance.LONG ? quote.getBid() : quote.getAsk());
    }

    @Override
    public Quote getQuote(Instrument pair) {
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
    public boolean isOpen() {
        return marketEngine.isAvailable();
    }

    @Override
    public boolean isOpen(LocalDate time) {
        return marketEngine.isAvailable(time);
    }

    @Override
    public OrderRequest getOrder(OrderRequest order) {
        return marketEngine.getOrder(order);
    }

    @Override
    public void openPosition(ForexTrader trader, Instrument pair, @Nullable Double limit) {

        Set<ForexPosition> positions = trader.getPortfolio().getPositions();
        Preconditions.checkArgument(positions.isEmpty(), "Currently only one open position is allowed at a time");

        // Open a long position on USD/EUR to simulate a short position for EUR/USD
        OrderRequest submitted;
        if (limit == null) {
            BuyMarketOrder order = Orders.buyMarketOrder(1, pair);
            submitted = marketEngine.submit(this, order);
        } else {
            BuyLimitOrder order = Orders.buyLimitOrder(1, pair, limit);
            submitted = marketEngine.submit(this, order);
        }
        orderSubmitted(trader, submitted);
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
