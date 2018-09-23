package forex.market

import forex.broker.Trade
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import java.time.LocalDateTime

import static forex.market.Instrument.USDEUR
import static java.time.Month.SEPTEMBER

@SpringBootTest
class TradeRepositorySpec extends Specification {

    @SpringBootConfiguration
    @Import(PersistenceConfig.class)
    static class SpecConfig {
    }

    @Autowired
    TradeRepository repo

    def 'should be able to save and retrieve trade summary'() {

        def entity = new Trade('309', '1', USDEUR, 86239L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 0), null,
                3, 3, 6L, 0L, 0L, 86238L, null, 0L, LocalDateTime.of(2018, SEPTEMBER, 7, 07, 45, 11, 0))

        when: 'a trade summary is saved'
        repo.save(entity)

        def retrieved = repo.findById(entity.id).get()

        then: 'it can be retrieved'
        retrieved == entity
    }

}
