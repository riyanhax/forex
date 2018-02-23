package market;

import instrument.CurrencyPair;
import instrument.CurrencyPairHistory;
import instrument.CurrencyPairService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import simulator.TimeAware;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
class ForexMarket implements TimeAware {

    private static final Logger LOG = LoggerFactory.getLogger(ForexMarket.class);

    private final CurrencyPairService currencyPairService;

    @Autowired
    public ForexMarket(CurrencyPairService currencyPairService) {
        this.currencyPairService = currencyPairService;
    }

    @Override
    public void advanceTime(LocalDateTime previous, LocalDateTime now) {
        CurrencyPairHistory data = currencyPairService.getData(CurrencyPair.EURUSD, now.atZone(ZoneId.systemDefault()));

        LOG.info("Time: {}\n\tEUR/USD Open: {}, High: {}, Low: {}, Close: {}", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                data.open, data.high, data.low, data.close);
    }

}
