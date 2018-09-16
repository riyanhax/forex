package simulator;

import broker.BrokerConfig;
import broker.LiveTraders;
import market.ForexMarket;
import market.InstrumentDataRetriever;
import market.InstrumentHistoryService;
import market.MarketConfig;
import market.MarketEngine;
import market.MarketTime;
import market.PersistenceConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import trader.ForexTrader;
import trader.Trader;
import trader.TraderConfig;
import trader.TradingStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableConfigurationProperties(SimulatorProperties.class)
@ComponentScan(basePackageClasses = {SimulatorConfig.class, PersistenceConfig.class,
        BrokerConfig.class, TraderConfig.class, MarketConfig.class})
public class SimulatorConfig {

    @Bean
    public MarketTime clock(SimulatorProperties simulatorProperties) {
        return new SimulatorClock(simulatorProperties);
    }

    @Bean
    InstrumentDataRetriever mockInstrumentDataRetriever() {
        return () -> {
            // Does nothing since this uses history data
        };
    }

    @Bean
    SimulatorContext context(SimulatorProperties simulatorProperties, MarketTime clock,
                             InstrumentHistoryService instrumentHistoryService,
                             SequenceService sequenceService,
                             TradeService tradeService,
                             MarketEngine marketEngine) {
        return new SimulatorContextImpl(clock, instrumentHistoryService, sequenceService,
                tradeService, marketEngine, simulatorProperties);
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

    @Bean
    public MarketEngine forexEngine(ForexMarket market, MarketTime clock) {
        return MarketEngine.create(market, clock);
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
