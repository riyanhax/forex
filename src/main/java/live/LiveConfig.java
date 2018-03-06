package live;

import market.InstrumentHistoryService;
import market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import trader.TradingStrategy;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Configuration
@ComponentScan
public class LiveConfig {

    private static final Logger LOG = LoggerFactory.getLogger(LiveConfig.class);

    @Bean
    LiveTraders trader(OandaProperties properties, MarketTime clock, InstrumentHistoryService instrumentHistoryService) {

        List<OandaTrader> traders = properties.getTraders().stream().map(it -> {
            String account = it.getAccount();
            TradingStrategy strategy = it.getStrategy();
            LOG.info("Using trading strategy: {}", strategy);

            return new OandaTrader(account, strategy, clock, instrumentHistoryService);
        }).collect(toList());

        return new LiveTraders(traders);
    }
}
