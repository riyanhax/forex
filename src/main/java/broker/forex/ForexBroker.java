package broker.forex;

import broker.Position;
import broker.Quote;
import market.forex.Instrument;
import market.order.OrderRequest;
import simulator.Simulation;
import trader.Trader;

import javax.annotation.Nullable;

public interface ForexBroker {
    void init(Simulation simulation);

    void processUpdates();

    ForexPortfolioValue getPortfolioValue(Trader trader);

    Quote getQuote(Instrument pair);

    void orderCancelled(OrderRequest filled);

    void orderFilled(OrderRequest filled);

    boolean isOpen();

    void openPosition(Trader trader, Instrument pair, int units, @Nullable Double limit);

    void closePosition(Trader trader, Position position, @Nullable Double limit);

    OrderRequest getOrder(OrderRequest order);
}
