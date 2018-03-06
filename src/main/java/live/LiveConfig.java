package live;

import market.InstrumentHistoryService;
import market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import trader.ForexTrader;
import trader.OpenRandomPosition;

@Configuration
@ComponentScan
public class LiveConfig {

    private static final Logger LOG = LoggerFactory.getLogger(LiveConfig.class);

    @Bean
    OandaTrader trader(MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        // TODO: Use a better trader when possible
        ForexTrader trader = new OpenRandomPosition(clock, instrumentHistoryService);

        LOG.info("Using trader: {}", trader.getClass().getName());

        return new OandaTrader(trader);
    }
}
