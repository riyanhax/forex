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

@Service
class SimulatorImpl implements Simulator {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatorImpl.class);

    private final CurrencyPairService currencyPairService;

    private LocalDateTime time;

    @Autowired
    public SimulatorImpl(CurrencyPairService currencyPairService) {
        this.currencyPairService = currencyPairService;
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
    }

    void nextMinute(Simulation simulation) {
        if (time.equals(simulation.endTime)) {
            throw new IllegalStateException("Can't advance beyond the end of the simulation!");
        }

        CurrencyPairHistory data = currencyPairService.getData(CurrencyPair.EURUSD, time.atZone(ZoneId.systemDefault()));
        LOG.info("Time: {}\n\tEUR/USD Open: {}, High: {}, Low: {}, Close: {}", time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                data.open, data.high, data.low, data.close);

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
