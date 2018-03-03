package simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.MINUTES;
import static market.MarketTime.formatTimestamp;

@Service
class SimulatorImpl implements Simulator {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatorImpl.class);

    private final SimulatorForexBroker broker;
    private final SimulatorClockImpl clock;

    @Autowired
    public SimulatorImpl(SimulatorClockImpl clock,
                         SimulatorForexBroker broker) {
        this.clock = clock;
        this.broker = broker;
    }

    @Override
    public void run(Simulation simulation) {
        init(simulation);

        while (clock.now().isBefore(simulation.endTime)) {
            nextMinute(simulation);
        }

        broker.done();
    }

    void init(Simulation simulation) {
        clock.init(simulation.startTime);

        broker.init(simulation);
        broker.processUpdates();
    }

    void nextMinute(Simulation simulation) {
        LocalDateTime previous = clock.now();
        if (!previous.isBefore(simulation.endTime)) {
            throw new IllegalStateException("Can't advance beyond the end of the simulation!");
        }

        clock.advance(1, MINUTES);

        LocalDateTime now = clock.now();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Time: {}", formatTimestamp(now));
        } else {
            if (now.getHour() == 0 && now.getMinute() == 0 && now.getSecond() == 0) {
                LOG.info("Time: {}", formatTimestamp(now));
            }
        }

        broker.processUpdates();

        if (simulation.millisDelayBetweenMinutes > 0) {
            try {
                Thread.sleep(simulation.millisDelayBetweenMinutes);
            } catch (InterruptedException e) {
                LOG.error("Interrupted trying to wait!", e);
            }
        }
    }
}
