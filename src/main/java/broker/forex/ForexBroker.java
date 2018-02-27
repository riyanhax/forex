package broker.forex;

import broker.Quote;
import market.forex.Instrument;
import market.order.BuyLimitOrder;
import market.order.BuyMarketOrder;
import market.order.OrderRequest;
import market.order.SellLimitOrder;
import market.order.SellMarketOrder;
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

    OrderRequest getOrder(OrderRequest order);

    OrderRequest submit(Trader trader, BuyMarketOrder order);

    OrderRequest submit(Trader trader, BuyLimitOrder order);

    OrderRequest submit(Trader trader, SellMarketOrder order);

    OrderRequest submit(Trader trader, SellLimitOrder order);
}
