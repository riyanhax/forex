package simulator;

import broker.Broker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
class SimulatorImpl implements Simulator {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatorImpl.class);

    private final List<Broker> brokers;
    private SimulatorClockImpl clock;

    @Autowired
    public SimulatorImpl(SimulatorClockImpl clock, List<Broker> brokers) {
        this.clock = clock;
        this.brokers = brokers;
    }

    @Override
    public void run(Simulation simulation) {
        init(simulation);

        while (clock.now().isBefore(simulation.endTime)) {
            nextMinute(simulation);
        }
    }

    void init(Simulation simulation) {
        clock.init(simulation.startTime);
        brokers.forEach(TimeAware::processUpdates);
    }

    void nextMinute(Simulation simulation) {
        LocalDateTime previous = clock.now();
        if (!previous.isBefore(simulation.endTime)) {
            throw new IllegalStateException("Can't advance beyond the end of the simulation!");
        }

        clock.advance(1, MINUTES);

        LOG.info("Time: {}", clock.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));

        brokers.forEach(TimeAware::processUpdates);

        if (simulation.millisDelayBetweenMinutes > 0) {
            try {
                Thread.sleep(simulation.millisDelayBetweenMinutes);
            } catch (InterruptedException e) {
                LOG.error("Interrupted trying to wait!", e);
            }
        }
    }
}
