package market;

import market.forex.ForexMarket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import simulator.SimulatorClock;

@Configuration
@ComponentScan
public class MarketConfig {

    @Bean
    public MarketEngine forexEngine(ForexMarket market, SimulatorClock clock) {
        return MarketEngine.create(market, clock);
    }

}
