package trader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import simulator.SimulatorClock;

@Configuration
@ComponentScan
public class TraderConfig {

    @Bean
    public DoNothingTrader trader1(SimulatorClock clock) {
        return new DoNothingTrader(clock);
    }

    @Bean
    public DoNothingTrader trader2(SimulatorClock clock) {
        return new DoNothingTrader(clock);
    }

    @Bean
    public DoNothingTrader trader3(SimulatorClock clock) {
        return new DoNothingTrader(clock);
    }

    @Bean
    public DoNothingTrader trader4(SimulatorClock clock) {
        return new DoNothingTrader(clock);
    }

    @Bean
    public DoNothingTrader trader5(SimulatorClock clock) {
        return new DoNothingTrader(clock);
    }

}
