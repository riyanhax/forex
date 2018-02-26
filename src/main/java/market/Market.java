package market;

import market.forex.Instrument;
import simulator.SimulationAware;

public interface Market extends SimulationAware {

    double getPrice(Instrument instrument);

    boolean isAvailable();

    void processUpdates();
}
