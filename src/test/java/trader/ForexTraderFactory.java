package trader;

import market.InstrumentHistoryService;
import market.MarketTime;
import simulator.Simulation;
import simulator.SimulatorForexTrader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface ForexTraderFactory {

    TradingStrategy create();

    default Collection<SimulatorForexTrader> createInstances(Simulation simulation, MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        List<SimulatorForexTrader> traders = new ArrayList<>();
        for (int i = 0; i < simulation.instancesPerTraderType; i++) {
            traders.add(new SimulatorForexTrader(create(), clock, instrumentHistoryService));
        }
        return traders;
    }
}
