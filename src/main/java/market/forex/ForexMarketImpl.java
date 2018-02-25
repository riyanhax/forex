package market.forex;

import market.OHLC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import simulator.SimulatorClock;

import java.util.Optional;

@Service
class ForexMarketImpl implements ForexMarket {

    private static final Logger LOG = LoggerFactory.getLogger(ForexMarketImpl.class);

    private final SimulatorClock clock;
    private final CurrencyPairHistoryService currencyPairService;

    @Autowired
    public ForexMarketImpl(SimulatorClock clock, CurrencyPairHistoryService currencyPairService) {
        this.clock = clock;
        this.currencyPairService = currencyPairService;
    }

    @Override
    public void processUpdates() {

        LOG.info("\tUpdating instrument quote data");

        Optional<CurrencyPairHistory> history = currencyPairService.getData(CurrencyPair.EURUSD, clock.now());
        history.ifPresent(h -> {
            OHLC data = h.getOHLC();

            LOG.info("\tEUR/USD Open: {}, High: {}, Low: {}, Close: {}", data.open, data.high, data.low, data.close);
        });
    }

    @Override
    public double getPrice(CurrencyPair instrument) {
        Optional<CurrencyPairHistory> history = currencyPairService.getData(instrument, clock.now());

        return history.get().getOHLC().open;
    }

    @Override
    public boolean isAvailable() {
        return currencyPairService.getData(CurrencyPair.EURUSD, clock.now()).isPresent();
    }
}
