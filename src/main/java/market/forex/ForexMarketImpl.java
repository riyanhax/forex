package market.forex;

import market.OHLC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import simulator.Simulation;
import market.MarketTime;

import java.time.LocalDate;
import java.util.Optional;

@Service
class ForexMarketImpl implements ForexMarket {

    private static final Logger LOG = LoggerFactory.getLogger(ForexMarketImpl.class);

    private final MarketTime clock;
    private final CurrencyPairHistoryService currencyPairService;

    @Autowired
    public ForexMarketImpl(MarketTime clock, CurrencyPairHistoryService currencyPairService) {
        this.clock = clock;
        this.currencyPairService = currencyPairService;
    }

    @Override
    public void init(Simulation simulation) {
    }

    @Override
    public void processUpdates() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("\tUpdating instrument quote data");

            Optional<CurrencyPairHistory> history = currencyPairService.getData(Instrument.EURUSD, clock.now());
            history.ifPresent(h -> {
                OHLC data = h.getOHLC();

                LOG.debug("\tEUR/USD Open: {}, High: {}, Low: {}, Close: {}", data.open, data.high, data.low, data.close);
            });
        }
    }

    @Override
    public double getPrice(Instrument instrument) {
        Optional<CurrencyPairHistory> history = currencyPairService.getData(instrument, clock.now());

        return history.get().getOHLC().open;
    }

    @Override
    public boolean isAvailable() {
        return currencyPairService.getData(Instrument.EURUSD, clock.now()).isPresent();
    }

    @Override
    public boolean isAvailable(LocalDate date) {
        return currencyPairService.getAvailableDays(Instrument.EURUSD, date.getYear()).contains(date);
    }
}
