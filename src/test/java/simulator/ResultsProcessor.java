package simulator;

import broker.LiveTraders;

public interface ResultsProcessor {

    void done(LiveTraders traders, SimulatorContext context, SimulatorProperties properties);

}
