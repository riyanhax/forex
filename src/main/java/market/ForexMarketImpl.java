package market;

import instrument.CurrencyPair;
import instrument.CurrencyPairHistory;
import instrument.CurrencyPairService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
class ForexMarketImpl implements ForexMarket {

    private static final Logger LOG = LoggerFactory.getLogger(ForexMarketImpl.class);

    private final CurrencyPairService currencyPairService;

    @Autowired
    public ForexMarketImpl(CurrencyPairService currencyPairService) {
        this.currencyPairService = currencyPairService;
    }

    @Override
    public void advanceTime(LocalDateTime previous, LocalDateTime now) {
        LOG.info("\tUpdating instrument quote data");

        CurrencyPairHistory data = currencyPairService.getData(CurrencyPair.EURUSD, now.atZone(ZoneId.systemDefault()));

        LOG.info("\tEUR/USD Open: {}, High: {}, Low: {}, Close: {}", data.open, data.high, data.low, data.close);
    }

}
