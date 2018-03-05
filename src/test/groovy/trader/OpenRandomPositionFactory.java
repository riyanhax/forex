package trader;

import market.InstrumentHistoryService;
import market.MarketTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenRandomPositionFactory implements ForexTraderFactory {

    private final MarketTime clock;
    private final InstrumentHistoryService instrumentHistoryService;

    @Autowired
    public OpenRandomPositionFactory(MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        this.clock = clock;
        this.instrumentHistoryService = instrumentHistoryService;
    }

    @Override
    public ForexTrader create() {
        return new OpenRandomPosition(clock, instrumentHistoryService);
    }
}
