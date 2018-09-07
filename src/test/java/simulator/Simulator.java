package simulator;

import broker.ForexBroker;
import live.LiveTraders;
import market.BaseWatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
class Simulator extends BaseWatcher<SimulatorClock, ForexBroker> {

    private final SimulatorProperties simulatorProperties;
    private final SimulatorContext context;
    private final LiveTraders traders;
    private final List<ResultsProcessor> resultsProcessors;

    @Autowired
    public Simulator(SimulatorProperties simulatorProperties,
                     SimulatorClock clock,
                     ForexBroker broker,
                     SimulatorContext context,
                     LiveTraders traders,
                     List<ResultsProcessor> resultsProcessors) {
        super(clock, broker);

        this.simulatorProperties = simulatorProperties;
        this.context = context;
        this.traders = traders;
        this.resultsProcessors = resultsProcessors;
    }

    @Override
    public void run() throws Exception {
        super.run();

        done();
    }

    @Override
    public boolean keepGoing(LocalDateTime now) {
        return now.isBefore(simulatorProperties.getEndTime());
    }

    @Override
    public long millisUntilNextInterval() {
        return simulatorProperties.getMillisDelayBetweenMinutes();
    }

    @Override
    protected void nextMinute() throws Exception {
        LocalDateTime previous = clock.now();
        if (!previous.isBefore(simulatorProperties.getEndTime())) {
            throw new IllegalStateException("Can't advance beyond the end of the simulation!");
        }

        clock.advance(1, MINUTES);

        if (!context.isAvailable()) {
            return;
        }

        context.beforeTraders();

        super.nextMinute();

        context.afterTraders();
    }

    @Override
    public boolean logTime(LocalDateTime now) {
        return now.getMinute() == 0 && now.getHour() == 0 && now.getSecond() == 0;
    }

    private void done() {
        this.resultsProcessors.forEach(it -> it.done(traders, context, simulatorProperties));
    }

}
