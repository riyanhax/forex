package broker.forex;

import broker.Quote;
import market.forex.Instrument;
import market.order.OrderRequest;
import simulator.Simulation;
import trader.Trader;

public interface ForexBroker {
    void init(Simulation simulation);

    void processUpdates();

    ForexPortfolioValue getPortfolioValue(Trader trader);

    Quote getQuote(Instrument pair);

    void orderCancelled(OrderRequest filled);

    void orderFilled(OrderRequest filled);

    boolean isOpen();
}
