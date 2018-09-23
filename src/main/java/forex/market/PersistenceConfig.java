package forex.market;

import forex.broker.Account;
import forex.broker.TradeSummary;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableAutoConfiguration
@EnableJpaRepositories
@Configuration
@EntityScan(basePackageClasses = {Account.class, InstrumentCandle.class, TradeSummary.class})
public class PersistenceConfig {
}
