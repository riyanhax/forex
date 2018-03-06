package market;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class MarketConfig {

    @Bean
    public MarketEngine forexEngine(ForexMarket market, MarketTime clock) {
        return MarketEngine.create(market, clock);
    }

}