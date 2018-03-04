package market;

import broker.ForexBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static market.MarketTime.formatTimestamp;

public abstract class BaseWatcher<CLOCK extends MarketTime, BROKER extends ForexBroker> implements Watcher {

    private static final Logger LOG = LoggerFactory.getLogger(BaseWatcher.class);

    protected final CLOCK clock;
    protected final BROKER broker;

    public BaseWatcher(CLOCK clock,
                       BROKER broker) {
        this.clock = clock;
        this.broker = broker;
    }

    @Override
    public void run() throws Exception {
        init();

        while (keepGoing(clock.now())) {
            nextMinute();
        }
    }

    protected void init() throws Exception {
        broker.processUpdates();
    }

    protected void nextMinute() throws Exception {

        LocalDateTime now = clock.now();
        if (logTime(now)) {
            LOG.info("Time: {}", formatTimestamp(now));
        }

        broker.processUpdates();

        long millisUntilNextInterval = millisUntilNextInterval();
        if (millisUntilNextInterval > 0) {
            try {
                Thread.sleep(millisUntilNextInterval);
            } catch (InterruptedException e) {
                LOG.error("Interrupted trying to wait!", e);
            }
        }
    }
}
