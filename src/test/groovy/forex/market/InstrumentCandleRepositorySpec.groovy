package forex.market

import forex.broker.CandlestickGranularity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import java.time.LocalDateTime

import static java.time.Month.SEPTEMBER

@SpringBootTest
class InstrumentCandleRepositorySpec extends Specification {

    @SpringBootConfiguration
    @Import(PersistenceConfig.class)
    static class SpecConfig {
    }

    @Autowired
    InstrumentCandleRepository repo

    def 'should be able to save and retrieve candle data'() {

        def id = new InstrumentCandleType(instrument: Instrument.EURUSD, time: LocalDateTime.of(2018, SEPTEMBER, 15, 14, 58, 0),
                granularity: CandlestickGranularity.M1)
        def entity = new InstrumentCandle(id: id,
                midOpen: 30L, midHigh: 50L, midLow: 20L, midClose: 40L,
                openSpread: 14L, highSpread: 14L, lowSpread: 14L, closeSpread: 14L)

        when: 'a candlestick is saved'
        repo.save(entity)

        def retrieved = repo.findById(id).get()

        then: 'it can be retrieved'
        retrieved == entity
    }
}
