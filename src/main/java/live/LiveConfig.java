package live;

import market.InstrumentHistoryService;
import market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import trader.ForexTrader;
import trader.SmarterRandomPositionFactory;

@Configuration
@ComponentScan
public class LiveConfig {

    private static final Logger LOG = LoggerFactory.getLogger(LiveConfig.class);

    @Bean
    ForexTrader trader(MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        ForexTrader trader = new SmarterRandomPositionFactory(clock, instrumentHistoryService).create();

        LOG.info("Using trader: {}", trader.getClass().getName());

        return trader;
    }
}
