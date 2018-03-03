package simulator;

import broker.BrokerConfig;
import market.MarketConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import trader.TraderConfig;

@SpringBootApplication(scanBasePackageClasses = {Simulator.class, MarketConfig.class,
        BrokerConfig.class, TraderConfig.class})
public class SimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulatorApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(Simulator simulator) {
        return args -> simulator.run(new Simulation());
    }

}
