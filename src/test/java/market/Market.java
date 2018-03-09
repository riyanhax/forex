package market;

import simulator.SimulationAware;

import java.time.LocalDate;

public interface Market extends SimulationAware {

    long getPrice(Instrument instrument);

    boolean isAvailable();

    boolean isAvailable(LocalDate date);

    void processUpdates();
}
