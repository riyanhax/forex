package forex.market

import forex.broker.MarketOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static forex.market.Instrument.EURUSD
import static java.time.LocalDateTime.of as ldt
import static java.time.Month.SEPTEMBER

@SpringBootTest
class AccountOrderServiceImplSpec extends Specification {

    @SpringBootConfiguration
    @Import(PersistenceConfig.class)
    @ComponentScan(basePackageClasses = AccountOrderService.class, useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AccountOrderService.class))
    static class SpecConfig {
    }

    @Autowired
    AccountOrderService service

    def 'should be able to save and retrieve market order'() {

        def entity = new MarketOrder('1006', '101-001-1775714-001', ldt(2018, SEPTEMBER, 7, 10, 56, 46), null, null, EURUSD, 7)

        when: 'a trade summary is saved'
        def persisted = service.upsert(entity)
        def retrieved = service.findMarketOrder(entity.orderId, entity.accountId)

        then: 'it can be retrieved'
        retrieved == entity
        retrieved == persisted
    }

    // TODO: Test for saveIfNotExists not overwriting existing
    // TODO: Test for upsert overwriting existing

}
