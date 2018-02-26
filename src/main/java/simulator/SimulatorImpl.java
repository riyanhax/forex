package simulator;

import market.MarketEngine;
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

    private final List<MarketEngine> marketEngines;
    private final SimulatorClockImpl clock;

    @Autowired
    public SimulatorImpl(SimulatorClockImpl clock,
                         List<MarketEngine> marketEngines) {
        this.clock = clock;
        this.marketEngines = marketEngines;
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
        marketEngines.forEach(it -> it.init(simulation));
        marketEngines.forEach(MarketEngine::processUpdates);
    }

    void nextMinute(Simulation simulation) {
        LocalDateTime previous = clock.now();
        if (!previous.isBefore(simulation.endTime)) {
            throw new IllegalStateException("Can't advance beyond the end of the simulation!");
        }

        clock.advance(1, MINUTES);

        LOG.info("Time: {}", clock.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));

        marketEngines.forEach(MarketEngine::processUpdates);

        if (simulation.millisDelayBetweenMinutes > 0) {
            try {
                Thread.sleep(simulation.millisDelayBetweenMinutes);
            } catch (InterruptedException e) {
                LOG.error("Interrupted trying to wait!", e);
            }
        }
    }
}
