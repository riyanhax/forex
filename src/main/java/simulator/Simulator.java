package simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Simulator {

    private static final Logger LOG = LoggerFactory.getLogger(Simulator.class);

    private final Simulation simulation;
    private LocalDateTime time;

    Simulator(Simulation simulation) {
        this.simulation = simulation;

        time = simulation.startTime;
    }

    public void run() {
        while (time.isBefore(simulation.endTime)) {
            LOG.info("Simulated time: {}", time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            LOG.info("Wall time: {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            nextMinute();
        }
    }

    void nextMinute() {
        if (time.equals(simulation.endTime)) {
            throw new IllegalStateException("Can't advance beyond the end of the simulation!");
        }

        if (simulation.millisDelayBetweenMinutes > 0) {
            try {
                Thread.sleep(simulation.millisDelayBetweenMinutes);
            } catch (InterruptedException e) {
                LOG.error("Interrupted trying to wait!", e);
            }
        }

        time = time.plusMinutes(1L);
    }

    LocalDateTime currentTime() {
        return time;
    }
}
