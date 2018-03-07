package simulator;

import broker.BrokerConfig;
import market.MarketConfig;
import market.Watcher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import trader.TraderConfig;
import trader.TradingStrategies;
import trader.TradingStrategy;

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
    public TradingStrategy randomPosition() {
        return TradingStrategies.OPEN_RANDOM_POSITION;
    }

    @Bean
    public TradingStrategy smarterRandomPosition() {
        return TradingStrategies.SMARTER_RANDOM_POSITION;
    }

}
