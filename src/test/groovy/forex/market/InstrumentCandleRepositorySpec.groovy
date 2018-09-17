package forex.market

import forex.broker.CandlestickGranularity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import spock.lang.Specification

import java.time.LocalDateTime

import static forex.market.Instrument.EURUSD
import static java.time.Month.MARCH
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

        def id = new InstrumentCandleType(instrument: EURUSD, time: LocalDateTime.of(2018, SEPTEMBER, 15, 14, 58, 0),
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

    @Sql('/history/instrument_candle_2017.sql')
    def 'should be able to query by instrument and time range'() {

        def actual = repo.findByIdInstrumentAndIdTimeBetweenOrderByIdTime(EURUSD, LocalDateTime.of(2017, MARCH, 3, 13, 23),
                LocalDateTime.of(2017, MARCH, 3, 13, 27))
                .collect { it.id.time }

        expect:
        actual == [
                LocalDateTime.of(2017, MARCH, 3, 13, 23),
                LocalDateTime.of(2017, MARCH, 3, 13, 24),
                LocalDateTime.of(2017, MARCH, 3, 13, 25),
                LocalDateTime.of(2017, MARCH, 3, 13, 26),
                LocalDateTime.of(2017, MARCH, 3, 13, 27)
        ]
    }

}
