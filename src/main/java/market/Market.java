package market;

import simulator.SimulationAware;

public interface Market<I extends Instrument> extends SimulationAware {

    double getPrice(I instrument);

    boolean isAvailable();
}
