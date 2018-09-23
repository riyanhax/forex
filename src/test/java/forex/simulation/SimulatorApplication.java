package forex.simulation;

import forex.market.MarketConfig;
import forex.market.PersistenceConfig;
import forex.market.Watcher;
import forex.simulator.SimulatorConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({SimulatorConfig.class, PersistenceConfig.class, MarketConfig.class})
class SimulatorApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "simulation");
        SpringApplication.run(SimulatorApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(Watcher watcher) {
        return args -> watcher.run();
    }
}
