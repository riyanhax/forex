package simulator;

import broker.ForexBroker;

public interface SimulatorForexBroker extends ForexBroker {
    void init(Simulation simulation);

    void done() throws Exception;
}
