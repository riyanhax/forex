package market;

import broker.Instrument;
import instrument.CurrencyPair;
import instrument.CurrencyPairHistory;
import instrument.CurrencyPairService;
import instrument.OHLC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import simulator.SimulatorClock;

import java.util.Random;

@Service
class ForexMarketImpl implements ForexMarket {

    private static final Logger LOG = LoggerFactory.getLogger(ForexMarketImpl.class);

    private final SimulatorClock clock;
    private final CurrencyPairService currencyPairService;

    @Autowired
    public ForexMarketImpl(SimulatorClock clock, CurrencyPairService currencyPairService) {
        this.clock = clock;
        this.currencyPairService = currencyPairService;
    }

    @Override
    public void processUpdates() {

        LOG.info("\tUpdating instrument quote data");

        CurrencyPairHistory history = currencyPairService.getData(CurrencyPair.EURUSD, clock.now());
        OHLC data = history.ohlc;

        LOG.info("\tEUR/USD Open: {}, High: {}, Low: {}, Close: {}", data.open, data.high, data.low, data.close);
    }

    @Override
    public double getPrice(Instrument instrument) {
        if (!CurrencyPair.class.isInstance(instrument)) {
            throw new IllegalStateException("Unknown instrument: " + instrument);
        }

        return currencyPairService.getData((CurrencyPair) instrument, clock.now()).ohlc.open + new Random().nextDouble();
    }
}
