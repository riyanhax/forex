package broker.forex;

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

    void openPosition(Trader trader, Instrument pair, @Nullable Double limit);

    void closePosition(Trader trader, ForexPosition position, @Nullable Double limit);

    OrderRequest getOrder(OrderRequest order);
}
