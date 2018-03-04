package live;

import market.ForexMarket;
import market.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import simulator.Simulation;

import java.time.LocalDate;

import static market.MarketTime.formatTimestamp;

@Service
class LiveForexMarket implements ForexMarket {

    private static final Logger LOG = LoggerFactory.getLogger(LiveForexMarket.class);

    @Override
    public double getPrice(Instrument instrument) {
        LOG.info("*** Retrieving price for {} from Oanda", instrument);

        return 0;
    }

    @Override
    public boolean isAvailable() {
        LOG.info("*** Checking if Oanda is available");

        return false;
    }

    @Override
    public boolean isAvailable(LocalDate date) {
        // TODO: Should only be in the simulator
        LOG.info("*** Checking if Oanda is available on {}", formatTimestamp(date.atStartOfDay()));

        return false;
    }

    @Override
    public void processUpdates() {
        LOG.info("*** Processing updates");

    }

    @Override
    public void init(Simulation simulation) {
        // TODO: Should only be in the simulator
        LOG.info("*** Init simulation");
    }
}
