package simulator;

import market.BaseWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
class SimulatorImpl extends BaseWatcher<SimulatorClockImpl, SimulatorForexBroker> {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatorImpl.class);

    private final Simulation simulation = new Simulation();

    @Autowired
    public SimulatorImpl(SimulatorClockImpl clock,
                         SimulatorForexBroker broker) {
        super(clock, broker);
    }

    @Override
    public void run() {
        super.run();

        broker.done();
    }

    @Override
    public boolean keepGoing(LocalDateTime now) {
        return now.isBefore(simulation.endTime);
    }

    @Override
    public long millisUntilNextInterval() {
        return simulation.millisDelayBetweenMinutes;
    }

    @Override
    protected void init() {
        clock.init(simulation.startTime);
        broker.init(simulation);

        super.init();
    }

    @Override
    protected void nextMinute() {
        LocalDateTime previous = clock.now();
        if (!previous.isBefore(simulation.endTime)) {
            throw new IllegalStateException("Can't advance beyond the end of the simulation!");
        }

        clock.advance(1, MINUTES);

        super.nextMinute();
    }
}
