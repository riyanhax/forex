package forex.market

import forex.broker.MarketOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static forex.market.Instrument.EURUSD
import static java.time.Month.SEPTEMBER
import static java.time.LocalDateTime.of as ldt

@SpringBootTest
class OrderRepositorySpec extends Specification {

    @SpringBootConfiguration
    @Import(PersistenceConfig.class)
    static class SpecConfig {
    }

    @Autowired
    OrderRepository repo

    def 'should be able to save and retrieve market order transaction'() {

        def entity = new MarketOrder('1006', '101-001-1775714-001', ldt(2018, SEPTEMBER, 7, 10, 56, 46), null, null, EURUSD, 7)

        when: 'a trade summary is saved'
        def persisted = repo.save(entity)
        def retrieved = repo.findById(entity.id).get()

        then: 'it can be retrieved'
        retrieved == entity
        retrieved == persisted
    }

}
