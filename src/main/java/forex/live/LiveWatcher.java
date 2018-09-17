package forex.live;

import forex.broker.Broker;
import forex.market.BaseWatcher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
class LiveWatcher extends BaseWatcher<SystemTime, Broker> {

    public LiveWatcher(SystemTime marketTime, Broker broker) {
        super(marketTime, broker);
    }

    @Override
    public boolean keepGoing(LocalDateTime now) {
        return true;
    }

    @Override
    public long millisUntilNextInterval() {
        return TimeUnit.MINUTES.toMillis(1);
    }

    @Override
    public boolean logTime(LocalDateTime now) {
        return true;
    }
}
