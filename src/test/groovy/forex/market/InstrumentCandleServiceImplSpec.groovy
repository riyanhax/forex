package forex.market

import com.google.common.collect.ImmutableSet
import forex.broker.CandlePrice
import forex.broker.Candlestick
import forex.broker.CandlestickData
import forex.broker.Context
import forex.broker.InstrumentCandlesResponse
import forex.config.InstrumentCandleServiceImplSpecConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.lang.Unroll

import javax.transaction.Transactional
import java.time.LocalDateTime

import static com.google.common.collect.Range.closed
import static forex.broker.CandlePrice.ASK
import static forex.broker.CandlePrice.MID
import static forex.broker.CandlestickGranularity.M1
import static forex.broker.CandlestickGranularity.W
import static forex.market.Instrument.EURUSD
import static java.time.LocalDateTime.of as ldt
import static java.time.Month.JANUARY
import static java.time.Month.SEPTEMBER

@SpringBootTest(classes = InstrumentCandleServiceImplSpecConfig)
@Transactional
class InstrumentCandleServiceImplSpec extends Specification {

    @Autowired
    Context context

    @Autowired
    InstrumentCandleRepository repo

    @Autowired
    InstrumentCandleService service

    def 'should retrieve and store one minute candles for the request range'() {

        def start = ldt(2017, SEPTEMBER, 10, 8, 30)
        def end = ldt(2017, SEPTEMBER, 10, 8, 32)
        def range = closed(start, end)

        when:
        service.retrieveAndStoreOneMinuteCandles(range)

        then: 'candles are retrieved'
        1 * context.instrumentCandles({
            it.instrument == EURUSD &&
                    it.price == ImmutableSet.of(CandlePrice.BID, MID, ASK) &&
                    it.granularity == M1 &&
                    it.from == start &&
                    it.to == end &&
                    it.includeFirst
        }) >> new InstrumentCandlesResponse(EURUSD, W, [
                new Candlestick(LocalDateTime.of(2017, SEPTEMBER, 10, 8, 30),
                        new CandlestickData(113685, 114443, 113003, 114346),
                        new CandlestickData(113745, 114456, 113016, 114406),
                        new CandlestickData(113715, 114450, 113010, 114376)),
                new Candlestick(LocalDateTime.of(2017, SEPTEMBER, 10, 8, 31),
                        new CandlestickData(114365, 116393, 113938, 116190),
                        new CandlestickData(114425, 116412, 113953, 116250),
                        new CandlestickData(114395, 116402, 113946, 116220)),
                new Candlestick(LocalDateTime.of(2017, SEPTEMBER, 10, 8, 32),
                        new CandlestickData(116126, 117330, 115836, 115989),
                        new CandlestickData(116181, 117345, 115851, 116049),
                        new CandlestickData(116154, 117337, 115844, 116019))])

        and: 'converted and stored'
        repo.findAll() == [
                new InstrumentCandle(id: new InstrumentCandleType(instrument: EURUSD, time: ldt(2017, SEPTEMBER, 10, 8, 30, 0), granularity: M1),
                        midOpen: 113715, midHigh: 114450, midLow: 113010, midClose: 114376,
                        openSpread: 60L, highSpread: 13L, lowSpread: 13L, closeSpread: 60L),
                new InstrumentCandle(id: new InstrumentCandleType(instrument: EURUSD, time: ldt(2017, SEPTEMBER, 10, 8, 31, 0), granularity: M1),
                        midOpen: 114395, midHigh: 116402, midLow: 113946, midClose: 116220,
                        openSpread: 60L, highSpread: 19L, lowSpread: 15L, closeSpread: 60L),
                new InstrumentCandle(id: new InstrumentCandleType(instrument: EURUSD, time: ldt(2017, SEPTEMBER, 10, 8, 32, 0), granularity: M1),
                        midOpen: 116154, midHigh: 117337, midLow: 115844, midClose: 116019,
                        openSpread: 55L, highSpread: 15L, lowSpread: 15L, closeSpread: 60L),
        ]
    }

    @Unroll
    def 'should return 01/02/2005 12:29 as the most recent stored date when there is no stored instrument candles: #expected'() {

        if (expected != ldt(2005, JANUARY, 2, 12, 29)) {
            repo.save(new InstrumentCandle(id: new InstrumentCandleType(instrument: EURUSD, time: ldt(2017, SEPTEMBER, 10, 8, 31, 0), granularity: M1),
                    midOpen: 114395, midHigh: 116402, midLow: 113946, midClose: 116220,
                    openSpread: 60L, highSpread: 19L, lowSpread: 15L, closeSpread: 60L))
        }

        def actual = service.findLatestStoredMinute()

        expect:
        actual == expected

        where:
        expected << [
                ldt(2005, JANUARY, 2, 12, 29),
                ldt(2017, SEPTEMBER, 10, 8, 31, 0)
        ]
    }

}
