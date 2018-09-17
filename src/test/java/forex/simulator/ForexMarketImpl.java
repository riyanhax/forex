package forex.simulator;

import forex.broker.CandlestickData;
import forex.market.ForexMarket;
import forex.market.Instrument;
import forex.market.InstrumentHistory;
import forex.market.InstrumentHistoryService;
import forex.market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
class ForexMarketImpl implements ForexMarket {

    private static final Logger LOG = LoggerFactory.getLogger(ForexMarketImpl.class);

    private final MarketTime clock;
    private final InstrumentHistoryService historyService;

    @Autowired
    public ForexMarketImpl(MarketTime clock, InstrumentHistoryService historyService) {
        this.clock = clock;
        this.historyService = historyService;
    }

    @Override
    public void processUpdates() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("\tUpdating instrument quote data");

            Optional<InstrumentHistory> history = historyService.getData(Instrument.EURUSD, clock.now());
            history.ifPresent(h -> {
                CandlestickData data = h.getOHLC();

                LOG.debug("\tEUR/USD Open: {}, High: {}, Low: {}, Close: {}", data.getO(), data.getH(), data.getL(), data.getC());
            });
        }
    }

    @Override
    public long getPrice(Instrument instrument) {
        Optional<InstrumentHistory> history = historyService.getData(instrument, clock.now());

        return history.get().getOHLC().getO();
    }

    @Override
    public boolean isAvailable() {
        return historyService.getData(Instrument.EURUSD, clock.now()).isPresent();
    }

    @Override
    public boolean isAvailable(LocalDate date) {
        return historyService.getAvailableDays(Instrument.EURUSD, date.getYear()).contains(date);
    }
}
