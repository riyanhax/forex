package simulator;

import market.BaseWatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
class SimulatorImpl extends BaseWatcher<SimulatorClockImpl, SimulatorForexBroker> {

    private final Simulation simulation;

    @Autowired
    public SimulatorImpl(Simulation simulation,
                  SimulatorClockImpl clock,
                  SimulatorForexBroker broker) {
        super(clock, broker);

        this.simulation = simulation;
    }

    @Override
    public void run() throws Exception {
        super.run();

        broker.done();
    }

    @Override
    public boolean keepGoing(LocalDateTime now) {
        return now.isBefore(simulation.getEndTime());
    }

    @Override
    public long millisUntilNextInterval() {
        return simulation.getMillisDelayBetweenMinutes();
    }

    @Override
    protected void init() throws Exception {
        clock.init(simulation.getStartTime());
        broker.init(simulation);

        super.init();
    }

    @Override
    protected void nextMinute() throws Exception {
        LocalDateTime previous = clock.now();
        if (!previous.isBefore(simulation.getEndTime())) {
            throw new IllegalStateException("Can't advance beyond the end of the simulation!");
        }

        clock.advance(1, MINUTES);

        super.nextMinute();
    }

    @Override
    public boolean logTime(LocalDateTime now) {
        return now.getMinute() == 0 && now.getHour() == 0 && now.getSecond() == 0;
    }
}
