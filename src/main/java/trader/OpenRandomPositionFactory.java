package trader;

import market.InstrumentHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import simulator.SimulatorClock;
import trader.forex.ForexTrader;
import trader.forex.ForexTraderFactory;

@Service
public class OpenRandomPositionFactory implements ForexTraderFactory {

    private final SimulatorClock clock;
    private final InstrumentHistoryService instrumentHistoryService;

    @Autowired
    public OpenRandomPositionFactory(SimulatorClock clock, InstrumentHistoryService instrumentHistoryService) {
        this.clock = clock;
        this.instrumentHistoryService = instrumentHistoryService;
    }

    @Override
    public ForexTrader create() {
        return new OpenRandomPosition(clock, instrumentHistoryService);
    }
}
