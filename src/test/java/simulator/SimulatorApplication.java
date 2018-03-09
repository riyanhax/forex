package simulator;

import broker.BrokerConfig;
import live.LiveTraders;
import live.Oanda;
import live.OandaTrader;
import market.InstrumentHistoryService;
import market.MarketConfig;
import market.MarketEngine;
import market.MarketTime;
import market.Watcher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import trader.TraderConfig;
import trader.TradingStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SpringBootApplication(
        scanBasePackageClasses = {SimulatorForexBroker.class, MarketConfig.class,
                BrokerConfig.class, TraderConfig.class},
        exclude = {EmbeddedServletContainerAutoConfiguration.class,
                WebMvcAutoConfiguration.class})
public class SimulatorApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "simulation");
        SpringApplication.run(SimulatorApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(Watcher watcher) {
        return args -> watcher.run();
    }

    @Bean
    public MarketTime clock(Simulation simulation) {
        return new SimulatorClockImpl(simulation);
    }

    @Bean
    SimulatorContext context(Simulation simulation, MarketTime clock,
                             InstrumentHistoryService instrumentHistoryService,
                             MarketEngine marketEngine) {

        marketEngine.init(simulation);

        return new SimulatorContext(clock, instrumentHistoryService, marketEngine, simulation);
    }

    @Bean
    Oanda broker(MarketTime clock, LiveTraders traders) {
        return new Oanda(clock, traders);
    }

    @Bean
    LiveTraders traders(Simulation simulation, MarketTime clock,
                        SimulatorContext context) {

        List<OandaTrader> traders = new ArrayList<>();
        simulation.getTradingStrategies().forEach(it -> {
            try {
                traders.addAll(createInstances(it, simulation, context, clock));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return new LiveTraders(traders);
    }

    private Collection<OandaTrader> createInstances(TradingStrategy tradingStrategy,
                                                    Simulation simulation,
                                                    SimulatorContext context,
                                                    MarketTime clock) throws Exception {
        List<OandaTrader> traders = new ArrayList<>();
        for (int i = 0; i < simulation.getInstancesPerTraderType(); i++) {
            traders.add(new OandaTrader(tradingStrategy.toString() + "-" + i, context, tradingStrategy, clock));
        }
        return traders;
    }
}
