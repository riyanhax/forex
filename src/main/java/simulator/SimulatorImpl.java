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
    public SimulatorImpl(SimulatorClockImpl clock,
                         SimulatorForexBroker broker) {
        this(new Simulation(), clock, broker);
    }

    SimulatorImpl(Simulation simulation,
                  SimulatorClockImpl clock,
                  SimulatorForexBroker broker) {
        super(clock, broker);

        this.simulation = simulation;
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

    @Override
    public boolean logTime(LocalDateTime now) {
        return now.getMinute() == 0 && now.getHour() == 0 && now.getSecond() == 0;
    }
}
