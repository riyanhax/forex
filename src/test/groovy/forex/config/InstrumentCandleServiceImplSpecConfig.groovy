package forex.config

import forex.broker.Context
import forex.market.InstrumentCandleRepository
import forex.market.InstrumentCandleService
import forex.market.InstrumentCandleServiceImpl
import forex.market.PersistenceConfig
import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import spock.mock.DetachedMockFactory

@SpringBootConfiguration
@Import(PersistenceConfig.class)
class InstrumentCandleServiceImplSpecConfig {

    private final DetachedMockFactory factory = new DetachedMockFactory()

    @Bean
    Context context() {
        factory.Mock(Context)
    }

    @Bean
    InstrumentCandleService service(Context context, InstrumentCandleRepository repo) {
        new InstrumentCandleServiceImpl(context, repo)
    }
}
