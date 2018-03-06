package live;

import market.InstrumentHistoryService;
import market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import trader.OpenRandomPosition;
import trader.TradingStrategy;

@Configuration
@ComponentScan
public class LiveConfig {

    private static final Logger LOG = LoggerFactory.getLogger(LiveConfig.class);

    @Bean
    OandaTrader trader(MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        // TODO: Use a better trading strategy when possible
        TradingStrategy trader = new OpenRandomPosition();

        LOG.info("Using trading strategy: {}", trader.getClass().getName());

        return new OandaTrader(trader, clock, instrumentHistoryService);
    }
}
