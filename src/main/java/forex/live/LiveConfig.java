package forex.live;

import forex.broker.Context;
import forex.broker.LiveTraders;
import forex.live.oanda.OandaContext;
import forex.market.DBHistoryDataService;
import forex.market.InstrumentCandleRepository;
import forex.market.InstrumentHistoryService;
import forex.market.MarketTime;
import forex.trader.ForexTrader;
import forex.trader.Trader;
import forex.trader.TraderConfiguration;
import forex.trader.TradingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan
public class LiveConfig {

    private static final Logger LOG = LoggerFactory.getLogger(LiveConfig.class);

    @Bean
    Context context(OandaProperties properties) {
        return OandaContext.create(properties.getApi().getEndpoint(), properties.getApi().getToken());
    }

    @Bean
    InstrumentHistoryService instrumentHistoryService(MarketTime clock, InstrumentCandleRepository repo) {
        return new DBHistoryDataService(clock, repo);
    }

    @Bean
    LiveTraders traders(Context ctx, OandaProperties properties, MarketTime clock) throws Exception {

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
