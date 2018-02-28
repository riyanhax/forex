package broker.forex;

import broker.BidAsk;
import broker.Quote;
import broker.Stance;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import market.MarketEngine;
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
import simulator.Simulation;
import simulator.SimulatorClock;
import trader.Trader;
import trader.forex.ForexTrader;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
class Oanda implements ForexBroker {

    private static final Logger LOG = LoggerFactory.getLogger(Oanda.class);

    private final SimulatorClock clock;
    private final MarketEngine marketEngine;
    private final List<ForexTrader> traders;
    private final Map<String, Trader> tradersByOrderId = new HashMap<>();
    private Simulation simulation;

    public Oanda(SimulatorClock clock, MarketEngine marketEngine, List<ForexTrader> traders) {
        this.clock = clock;
        this.marketEngine = marketEngine;
        this.traders = traders;
    }

    @Override
    public void init(Simulation simulation) {
        this.simulation = simulation;

        marketEngine.init(simulation);

        tradersByOrderId.clear();
    }

    @Override
    public void processUpdates() {

        marketEngine.processUpdates();

        if (isOpen()) {
            LOG.info("\tCheck pending orders");
            LOG.info("\tProcess transactions");

            traders.forEach(it -> it.processUpdates(this));
        }

        marketEngine.processUpdates();
    }

    @Override
    public ForexPortfolioValue getPortfolioValue(Trader trader) {
        ForexPortfolio portfolio = trader.getPortfolio();

        Set<ForexPosition> positions = portfolio.getPositions();
        Set<ForexPositionValue> positionValues = positions.stream()
                .map(it -> {
                    Quote quote = getQuote(it.getInstrument());
                    return new ForexPositionValue(it, it.getStance() == Stance.LONG ? quote.getBid() : quote.getAsk());
                })
                .collect(Collectors.toSet());

        return new ForexPortfolioValue(portfolio, clock.now(), positionValues);
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

        Trader trader = tradersByOrderId.get(filled.getId());
        ForexPortfolio oldPortfolio = trader.getPortfolio();
        ImmutableMap<Instrument, ForexPosition> positionsByInstrument = Maps.uniqueIndex(oldPortfolio.getPositions(), ForexPosition::getInstrument);
        Map<Instrument, ForexPosition> newPositions = new HashMap<>(positionsByInstrument);
        ForexPosition existingPosition = positionsByInstrument.get(instrument);
        double newPipsProfit = oldPortfolio.getPipsProfit();

        if (filled.isSellOrder()) {
            Objects.requireNonNull(existingPosition, "Go long on the inverse pair, instead of shorting the primary pair.");
            newPipsProfit += existingPosition.pipsProfit(price);
            newPositions.remove(instrument);
        } else if (filled.isBuyOrder()) {
            Preconditions.checkArgument(existingPosition == null, "Shouldn't have more than one position open for a pair at a time!");
            Preconditions.checkArgument(!positionsByInstrument.containsKey(instrument.getOpposite()), "Shouldn't have more than one position open for a pair at a time!");

            newPositions.put(instrument, new ForexPosition(instrument, Stance.LONG, price));
        }

        ForexPortfolio portfolio = new ForexPortfolio(newPipsProfit, new HashSet<>(newPositions.values()));

        trader.setPortfolio(portfolio);
    }

    @Override
    public void orderCancelled(OrderRequest cancelled) {
        Trader trader = tradersByOrderId.get(cancelled.getId());
        trader.cancelled(cancelled);
    }

    @Override
    public boolean isOpen() {
        return marketEngine.isAvailable();
    }

    @Override
    public OrderRequest getOrder(OrderRequest order) {
        return marketEngine.getOrder(order);
    }

    @Override
    public void openPosition(Trader trader, Instrument pair, @Nullable Double limit) {

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
    public void closePosition(Trader trader, ForexPosition position, @Nullable Double limit) {
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

    private OrderRequest orderSubmitted(Trader trader, OrderRequest submitted) {
        tradersByOrderId.put(submitted.getId(), trader);
        return submitted;
    }
}
