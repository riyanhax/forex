package trader;

import market.InstrumentHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import simulator.AppClock;
import trader.forex.ForexTrader;
import trader.forex.ForexTraderFactory;

@Service
public class SmarterRandomPositionFactory implements ForexTraderFactory {

    private final AppClock clock;
    private final InstrumentHistoryService instrumentHistoryService;

    @Autowired
    public SmarterRandomPositionFactory(AppClock clock, InstrumentHistoryService instrumentHistoryService) {
        this.clock = clock;
        this.instrumentHistoryService = instrumentHistoryService;
    }

    @Override
    public ForexTrader create() {
        return new SmarterRandomPosition(clock, instrumentHistoryService);
    }
}
