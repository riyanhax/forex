package trader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import simulator.SimulatorClock;
import trader.forex.ForexTrader;
import trader.forex.ForexTraderFactory;

@Service
public class OpenRandomPositionFactory implements ForexTraderFactory {

    private final SimulatorClock clock;

    @Autowired
    public OpenRandomPositionFactory(SimulatorClock clock) {
        this.clock = clock;
    }

    @Override
    public ForexTrader create() {
        return new OpenRandomPosition(clock);
    }
}
