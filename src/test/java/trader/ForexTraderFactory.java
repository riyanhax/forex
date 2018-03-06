package trader;

import simulator.Simulation;
import simulator.SimulatorForexTrader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface ForexTraderFactory {

    ForexTrader create();

    default Collection<SimulatorForexTrader> createInstances(Simulation simulation) {
        List<SimulatorForexTrader> traders = new ArrayList<>();
        for (int i = 0; i < simulation.instancesPerTraderType; i++) {
            traders.add(new SimulatorForexTrader(create()));
        }
        return traders;
    }
}
