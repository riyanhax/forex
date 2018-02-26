package market;

import broker.forex.ForexBroker;
import market.forex.CurrencyPair;
import market.forex.ForexMarket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import simulator.SimulatorClock;

@Configuration
@ComponentScan
public class MarketConfig {

    @Bean
    public MarketEngine<CurrencyPair> forexEngine(ForexBroker broker, ForexMarket market, SimulatorClock clock) {
        return MarketEngine.create(market, broker, clock);
    }

}
