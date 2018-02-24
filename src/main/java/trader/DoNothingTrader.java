package trader;

import broker.Broker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
class DoNothingTrader implements Trader {

    private static final Logger LOG = LoggerFactory.getLogger(DoNothingTrader.class);

    @Override
    public void advanceTime(LocalDateTime previous, LocalDateTime now, Broker broker) {
        LOG.info("Checking portfolio");
        LOG.info("Making orders");
        LOG.info("Closing orders");
    }
}
