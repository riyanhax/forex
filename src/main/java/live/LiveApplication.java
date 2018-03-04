package live;

import broker.BrokerConfig;
import market.MarketConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import trader.TraderConfig;

@SpringBootApplication(scanBasePackageClasses = {LiveConfig.class, MarketConfig.class,
        BrokerConfig.class, TraderConfig.class})
public class LiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiveApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(LiveWatcher watcher) {
        return args -> watcher.run();
    }

}
