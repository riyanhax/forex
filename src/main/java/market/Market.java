package market;

import market.forex.Instrument;
import simulator.SimulationAware;

import java.time.LocalDate;

public interface Market extends SimulationAware {

    double getPrice(Instrument instrument);

    boolean isAvailable();

    boolean isAvailable(LocalDate date);

    void processUpdates();
}
