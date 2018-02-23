package simulator;

import instrument.CurrencyPair;
import instrument.CurrencyPairHistory;
import instrument.CurrencyPairService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
class SimulatorImpl implements Simulator {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatorImpl.class);

    private final List<TimeAware> timeObservers;
    private LocalDateTime time;

    @Autowired
    public SimulatorImpl(List<TimeAware> timeObservers) {
        this.timeObservers = timeObservers;
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

        timeObservers.forEach(it -> it.advanceTime(null, time));
    }

    void nextMinute(Simulation simulation) {
        if (time.equals(simulation.endTime)) {
            throw new IllegalStateException("Can't advance beyond the end of the simulation!");
        }

        LocalDateTime previous = time;
        time = time.plusMinutes(1L);

        timeObservers.forEach(it -> it.advanceTime(previous, time));

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
