package simulation;

import market.Watcher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import simulator.SimulatorConfig;

@SpringBootApplication
@Import(SimulatorConfig.class)
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
