package simulator;

import broker.BrokerConfig;
import live.Broker;
import live.LiveTraders;
import live.Trader;
import market.InstrumentHistoryService;
import market.MarketConfig;
import market.MarketEngine;
import market.MarketTime;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import trader.ForexTrader;
import trader.TraderConfig;
import trader.TradingStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableConfigurationProperties(SimulatorProperties.class)
@ComponentScan(basePackageClasses = {SimulatorConfig.class, MarketConfig.class,
        BrokerConfig.class, TraderConfig.class})
public class SimulatorConfig {

    @Bean
    public MarketTime clock(SimulatorProperties simulatorProperties) {
        return new SimulatorClock(simulatorProperties);
    }

    @Bean
    SimulatorContext context(SimulatorProperties simulatorProperties, MarketTime clock,
                             InstrumentHistoryService instrumentHistoryService,
                             SequenceService sequenceService,
                             MarketEngine marketEngine) {
        return new SimulatorContextImpl(clock, instrumentHistoryService, sequenceService, marketEngine, simulatorProperties);
    }

    @Bean
    Broker broker(MarketTime clock, LiveTraders traders) {
        return new Broker(clock, traders);
    }

    @Bean
    LiveTraders traders(SimulatorProperties simulatorProperties, MarketTime clock,
                        SimulatorContext context) {

        List<ForexTrader> traders = new ArrayList<>();
        simulatorProperties.getTradingStrategies().forEach(it -> {
            try {
                traders.addAll(createInstances(it, simulatorProperties, context, clock));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return new LiveTraders(traders);
    }

    private Collection<ForexTrader> createInstances(TradingStrategy tradingStrategy,
                                                    SimulatorProperties simulatorProperties,
                                                    SimulatorContext context,
                                                    MarketTime clock) throws Exception {
        List<ForexTrader> traders = new ArrayList<>();
        for (int i = 0; i < simulatorProperties.getInstancesPerTraderType(); i++) {
            traders.add(new Trader(tradingStrategy.toString() + "-" + i, context, tradingStrategy, clock));
        }
        return traders;
    }
}
