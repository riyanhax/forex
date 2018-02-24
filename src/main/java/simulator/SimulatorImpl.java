package simulator;

import broker.Broker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
class SimulatorImpl implements Simulator {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatorImpl.class);

    private final List<Broker> brokers;
    private LocalDateTime time;

    @Autowired
    public SimulatorImpl(List<Broker> brokers) {
        this.brokers = brokers;
    }

    @Override
    public void run(Simulation simulation) {
        init(simulation);

        while (time.isBefore(simulation.endTime)) {

            nextMinute(simulation);
        }
    }

    void init(Simulation simulation) {
        time = simulation.startTime;

        brokers.forEach(it -> it.advanceTime(null, time));
    }

    void nextMinute(Simulation simulation) {
        if (time.equals(simulation.endTime)) {
            throw new IllegalStateException("Can't advance beyond the end of the simulation!");
        }


        LocalDateTime previous = time;
        time = time.plusMinutes(1L);

        LOG.info("Time: {}", time.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));

        brokers.forEach(it -> it.advanceTime(previous, time));

        if (simulation.millisDelayBetweenMinutes > 0) {
            try {
                Thread.sleep(simulation.millisDelayBetweenMinutes);
            } catch (InterruptedException e) {
                LOG.error("Interrupted trying to wait!", e);
            }
        }
    }

    LocalDateTime currentTime() {
        return time;
    }
}
