package forex.market

import forex.broker.MarketOrderTransaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Unroll

import static forex.market.Instrument.EURUSD
import static forex.market.Instrument.USDEUR
import static java.time.LocalDateTime.of as ldt
import static java.time.Month.SEPTEMBER

@SpringBootTest
class OrderRepositorySpec extends Specification {

    @SpringBootConfiguration
    @Import(PersistenceConfig.class)
    static class SpecConfig {
    }

    @Autowired
    OrderRepository repo

    @Unroll
    def 'should be able to save and retrieve market order transaction'() {

        when: 'a trade summary is saved'
        def persisted = repo.save(entity)
        def retrieved = repo.findById(entity.id).get()

        then: 'it can be retrieved'
        retrieved == entity
        retrieved == persisted

        where:
        entity << [
                new MarketOrderTransaction('1006', '101-001-1775714-001', ldt(2018, SEPTEMBER, 7, 10, 56, 46), EURUSD, 7),
                new MarketOrderTransaction('1006', '101-001-1775714-001', ldt(2018, SEPTEMBER, 7, 10, 56, 46), USDEUR, 7)
        ]
    }

}
