package live;

import broker.Context;
import live.oanda.OandaContext;
import market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import trader.ForexTrader;
import trader.TradingStrategy;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan
public class LiveConfig {

    private static final Logger LOG = LoggerFactory.getLogger(LiveConfig.class);

    @Bean
    LiveTraders traders(OandaProperties properties, MarketTime clock) throws Exception {

        Context ctx = OandaContext.create(properties.getApi().getEndpoint(), properties.getApi().getToken());

        List<ForexTrader> traders = new ArrayList<>();
        for (TraderConfiguration it : properties.getTraders()) {
            String account = it.getAccount();
            TradingStrategy strategy = it.getStrategy();

            LOG.info("Using trading strategy: {}", strategy);

            traders.add(new Trader(account, ctx, strategy, clock));
        }

        return new LiveTraders(traders);
    }
}
