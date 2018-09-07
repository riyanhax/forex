package simulator;

import live.LiveTraders;

public interface ResultsProcessor {

    void done(LiveTraders traders, SimulatorContext context, SimulatorProperties properties);

}
