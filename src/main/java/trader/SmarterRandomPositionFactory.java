package trader;

import market.InstrumentHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import market.MarketTime;
import trader.forex.ForexTrader;
import trader.forex.ForexTraderFactory;

@Service
public class SmarterRandomPositionFactory implements ForexTraderFactory {

    private final MarketTime clock;
    private final InstrumentHistoryService instrumentHistoryService;

    @Autowired
    public SmarterRandomPositionFactory(MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        this.clock = clock;
        this.instrumentHistoryService = instrumentHistoryService;
    }

    @Override
    public ForexTrader create() {
        return new SmarterRandomPosition(clock, instrumentHistoryService);
    }
}
