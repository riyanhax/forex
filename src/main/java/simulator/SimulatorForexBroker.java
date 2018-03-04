package simulator;

import broker.ForexBroker;
import market.order.OrderRequest;

public interface SimulatorForexBroker extends ForexBroker {
    void init(Simulation simulation);

    void orderCancelled(OrderRequest filled);

    void done();

    void orderFilled(OrderRequest filled);

    OrderRequest getOrder(OrderRequest order);
}
