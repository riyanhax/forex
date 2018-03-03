package simulator;

import broker.ForexBroker;
import market.order.OrderRequest;
import trader.forex.ForexTrader;

import java.util.Collection;

public interface SimulatorForexBroker extends ForexBroker {
    void init(Simulation simulation, Collection<ForexTrader> traders);

    void processUpdates();

    void orderCancelled(OrderRequest filled);

    void orderFilled(OrderRequest filled);

    OrderRequest getOrder(OrderRequest order);
}
