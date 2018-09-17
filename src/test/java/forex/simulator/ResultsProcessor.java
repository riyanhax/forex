package forex.simulator;

import forex.broker.LiveTraders;

public interface ResultsProcessor {

    void done(LiveTraders traders, SimulatorContext context, SimulatorProperties properties);

}
