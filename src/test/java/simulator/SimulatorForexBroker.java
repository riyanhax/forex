package simulator;

import broker.ForexBroker;
import market.OrderListener;

public interface SimulatorForexBroker extends ForexBroker, OrderListener {
    void init(Simulation simulation);

    void done();
}
