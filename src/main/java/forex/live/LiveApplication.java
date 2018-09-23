package forex.live;

import forex.broker.BrokerConfig;
import forex.market.PersistenceConfig;
import forex.trader.TraderConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
