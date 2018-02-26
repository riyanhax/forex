import broker.BrokerConfig;
import market.MarketConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import simulator.Simulation;
import simulator.Simulator;
import simulator.SimulatorConfig;
import trader.TraderConfig;

@SpringBootApplication(scanBasePackageClasses = {SimulatorConfig.class, MarketConfig.class,
        BrokerConfig.class, TraderConfig.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(Simulator simulator) {
        return args -> simulator.run(new Simulation());
    }
    
}
