package live;

import broker.Context;
import market.InstrumentHistoryService;
import market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import trader.TradingStrategy;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan
public class LiveConfig {

    private static final Logger LOG = LoggerFactory.getLogger(LiveConfig.class);

    @Bean
    LiveTraders traders(OandaProperties properties, MarketTime clock,
                        InstrumentHistoryService instrumentHistoryService) throws Exception {

        Context ctx = OandaContext.create(properties.getApi().getEndpoint(), properties.getApi().getToken());

        List<OandaTrader> traders = new ArrayList<>();
        for (TraderConfiguration it : properties.getTraders()) {
            String account = it.getAccount();
            TradingStrategy strategy = it.getStrategy();

            LOG.info("Using trading strategy: {}", strategy);

            traders.add(new OandaTrader(account, ctx, strategy, clock, instrumentHistoryService));
        }

        return new LiveTraders(traders);
    }
}
