package trader;

import simulator.Simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface ForexTraderFactory {

    ForexTrader create();

    default Collection<ForexTrader> createInstances(Simulation simulation) {
        List<ForexTrader> traders = new ArrayList<>();
        for (int i = 0; i < simulation.instancesPerTraderType; i++) {
            traders.add(create());
        }
        return traders;
    }
}
