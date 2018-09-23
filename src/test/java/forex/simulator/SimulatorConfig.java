package forex.simulator;

import forex.broker.BrokerConfig;
import forex.broker.LiveTraders;
import forex.market.ForexMarket;
import forex.market.InstrumentDataRetriever;
import forex.market.InstrumentHistoryService;
import forex.market.MarketConfig;
import forex.market.MarketEngine;
import forex.market.MarketTime;
import forex.market.OneMinuteCandleReader;
import forex.market.PersistenceConfig;
import forex.trader.ForexTrader;
import forex.trader.Trader;
import forex.trader.TraderConfig;
import forex.trader.TraderService;
import forex.trader.TradingStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableConfigurationProperties(SimulatorProperties.class)
@Import({PersistenceConfig.class, BrokerConfig.class, TraderConfig.class, MarketConfig.class})
@ComponentScan
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
    OneMinuteCandleReader candleDataReader() {
        return new CSVHistoryFileReader();
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
                        SimulatorContext context, TraderService traderService) {

        List<ForexTrader> traders = new ArrayList<>();
        simulatorProperties.getTradingStrategies().forEach(it -> {
            try {
                traders.addAll(createInstances(it, simulatorProperties, context, traderService, clock));
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
                                                    TraderService traderService,
                                                    MarketTime clock) throws Exception {
        List<ForexTrader> traders = new ArrayList<>();
        for (int i = 0; i < simulatorProperties.getInstancesPerTraderType(); i++) {
            traders.add(new Trader(tradingStrategy.toString() + "-" + i, context, traderService, tradingStrategy, clock));
        }
        return traders;
    }
}
