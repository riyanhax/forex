package forex.live;

import forex.broker.BrokerConfig;
import forex.market.PersistenceConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import forex.trader.TraderConfig;

@SpringBootApplication(
        scanBasePackageClasses = {LiveConfig.class, BrokerConfig.class, TraderConfig.class, PersistenceConfig.class})
public class LiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiveApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(LiveWatcher watcher) {
        return args -> watcher.run();
    }

}
