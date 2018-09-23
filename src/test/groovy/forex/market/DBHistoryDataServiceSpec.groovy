package forex.market

import forex.broker.CandlestickData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.io.ByteArrayResource
import org.springframework.jdbc.datasource.init.ScriptUtils
import spock.lang.Specification
import spock.lang.Unroll

import javax.sql.DataSource
import java.time.LocalDateTime

import static com.google.common.collect.Range.closed
import static forex.market.CandleTimeFrame.FIFTEEN_MINUTE
import static forex.market.CandleTimeFrame.FIVE_MINUTE
import static forex.market.CandleTimeFrame.FOUR_HOURS
import static forex.market.CandleTimeFrame.ONE_DAY
import static forex.market.CandleTimeFrame.ONE_HOUR
import static forex.market.CandleTimeFrame.ONE_MONTH
import static forex.market.CandleTimeFrame.ONE_WEEK
import static forex.market.Instrument.EURUSD
import static forex.market.Instrument.USDEUR
import static java.time.Month.FEBRUARY
import static java.time.Month.JANUARY

@SpringBootTest(classes = PersistenceConfig)
class DBHistoryDataServiceSpec extends Specification {

    static boolean loadedDB = false

    @SpringBootConfiguration
    @Import(PersistenceConfig.class)
    static class SpecConfig {
    }

    @Autowired
    InstrumentCandleRepository repo
    @Autowired
    DataSource dataSource

    MarketTime clock = Mock(MarketTime)

    DBHistoryDataService service

    def setup() {

        if (!loadedDB) { // Don't know why I can't use @Sql to load this
            def testSql = DBHistoryDataServiceSpec.class.getResource('/history/instrument_candle_2017.sql').text;
            ScriptUtils.executeSqlScript(dataSource.getConnection(), new ByteArrayResource(testSql.bytes));

            loadedDB = true
        }

        service = new DBHistoryDataService(clock, repo)
    }

    @Unroll
    def 'should rollup minute data and ranges correctly, instrument: #instrument, timeFrame: #timeFrame, range: #range'() {

        clock.now() >> LocalDateTime.now()

        def actual = service.getOHLC(timeFrame, instrument, range)

        expect:
        actual == expected

        where:
        instrument | timeFrame      | range                                                                                           | expected
        EURUSD     | FIVE_MINUTE    | closed(LocalDateTime.of(2017, JANUARY, 2, 17, 0), LocalDateTime.of(2017, JANUARY, 2, 17, 9))    | [
                (LocalDateTime.of(2017, JANUARY, 2, 17, 0)): new CandlestickData(104684L, 104711L, 104646L, 104652L),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 5)): new CandlestickData(104656L, 104688L, 104608L, 104612L),
        ]
        USDEUR     | FIVE_MINUTE    | closed(LocalDateTime.of(2017, JANUARY, 2, 17, 0), LocalDateTime.of(2017, JANUARY, 2, 17, 9))    | [
                (LocalDateTime.of(2017, JANUARY, 2, 17, 0)): new CandlestickData(95525, 95560, 95500, 95554),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 5)): new CandlestickData(95551, 95594, 95521, 95591),
        ]
        EURUSD     | FIFTEEN_MINUTE | closed(LocalDateTime.of(2017, JANUARY, 2, 17, 0), LocalDateTime.of(2017, JANUARY, 2, 17, 29))   | [
                (LocalDateTime.of(2017, JANUARY, 2, 17, 0)) : new CandlestickData(104684L, 104711L, 104567L, 104570L),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 15)): new CandlestickData(104570L, 104640L, 104568L, 104630L),
        ]
        USDEUR     | FIFTEEN_MINUTE | closed(LocalDateTime.of(2017, JANUARY, 2, 17, 0), LocalDateTime.of(2017, JANUARY, 2, 17, 29))   | [
                (LocalDateTime.of(2017, JANUARY, 2, 17, 0)) : new CandlestickData(95525, 95632, 95500, 95629),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 15)): new CandlestickData(95629, 95631, 95565, 95574),
        ]
        EURUSD     | ONE_HOUR       | closed(LocalDateTime.of(2017, JANUARY, 2, 17, 0), LocalDateTime.of(2017, JANUARY, 2, 18, 0))    | [
                (LocalDateTime.of(2017, JANUARY, 2, 17, 0)): new CandlestickData(104684L, 104711L, 104567L, 104575L),
                (LocalDateTime.of(2017, JANUARY, 2, 18, 0)): new CandlestickData(104577L, 104712L, 104572L, 104662L),
        ]
        USDEUR     | ONE_HOUR       | closed(LocalDateTime.of(2017, JANUARY, 2, 17, 0), LocalDateTime.of(2017, JANUARY, 2, 18, 0))    | [
                (LocalDateTime.of(2017, JANUARY, 2, 17, 0)): new CandlestickData(95525, 95632, 95500, 95625),
                (LocalDateTime.of(2017, JANUARY, 2, 18, 0)): new CandlestickData(95623, 95627, 95500, 95545),
        ]
        EURUSD     | FOUR_HOURS     | closed(LocalDateTime.of(2017, JANUARY, 2, 17, 0), LocalDateTime.of(2017, JANUARY, 2, 20, 0))    | [
                // This makes the candle time frame one hour earlier than available data, just like Oanda.
                // Oanda aligns candles from 1700 eastern (so 1600 central)
                (LocalDateTime.of(2017, JANUARY, 2, 16, 0)): new CandlestickData(104684L, 104808L, 104567L, 104758L),
                (LocalDateTime.of(2017, JANUARY, 2, 20, 0)): new CandlestickData(104761L, 104902L, 104709L, 104824L),
        ]
        USDEUR     | FOUR_HOURS     | closed(LocalDateTime.of(2017, JANUARY, 2, 17, 0), LocalDateTime.of(2017, JANUARY, 2, 20, 0))    | [
                // This makes the candle time frame one hour earlier than available data, just like Oanda.
                // Oanda aligns candles from 1700 eastern (so 1600 central)
                (LocalDateTime.of(2017, JANUARY, 2, 16, 0)): new CandlestickData(95525, 95632, 95412, 95458),
                (LocalDateTime.of(2017, JANUARY, 2, 20, 0)): new CandlestickData(95455, 95502, 95327, 95398),
        ]
        EURUSD     | ONE_DAY        | closed(LocalDateTime.of(2017, JANUARY, 2, 17, 0), LocalDateTime.of(2017, JANUARY, 3, 16, 0))    | [
                (LocalDateTime.of(2017, JANUARY, 2, 16, 0)): new CandlestickData(104684L, 104902L, 103406L, 104050L),
                (LocalDateTime.of(2017, JANUARY, 3, 16, 0)): new CandlestickData(104061L, 105000L, 103899L, 104892L),
        ]
        USDEUR     | ONE_DAY        | closed(LocalDateTime.of(2017, JANUARY, 2, 17, 0), LocalDateTime.of(2017, JANUARY, 3, 16, 0))    | [
                (LocalDateTime.of(2017, JANUARY, 2, 16, 0)): new CandlestickData(95525, 96706, 95327, 96107),
                (LocalDateTime.of(2017, JANUARY, 3, 16, 0)): new CandlestickData(96097, 96247, 95238, 95336),
        ]
        EURUSD     | ONE_WEEK       | closed(LocalDateTime.of(2017, JANUARY, 6, 16, 0), LocalDateTime.of(2017, JANUARY, 13, 16, 0))   | [
                (LocalDateTime.of(2017, JANUARY, 6, 16, 0)) : new CandlestickData(105304L, 106848L, 104539L, 106434L),
                (LocalDateTime.of(2017, JANUARY, 13, 16, 0)): new CandlestickData(106051L, 107194L, 105794L, 107012L),
        ]
        USDEUR     | ONE_WEEK       | closed(LocalDateTime.of(2017, JANUARY, 6, 16, 0), LocalDateTime.of(2017, JANUARY, 13, 16, 0))   | [
                (LocalDateTime.of(2017, JANUARY, 6, 16, 0)) : new CandlestickData(94963, 95658, 93590, 93954),
                (LocalDateTime.of(2017, JANUARY, 13, 16, 0)): new CandlestickData(94294, 94523, 93288, 93447),
        ]
        EURUSD     | ONE_MONTH      | closed(LocalDateTime.of(2017, JANUARY, 31, 16, 0), LocalDateTime.of(2017, FEBRUARY, 28, 16, 0)) | [
                (LocalDateTime.of(2017, JANUARY, 31, 16, 0)) : new CandlestickData(107980L, 108290L, 104938L, 105764L),
                (LocalDateTime.of(2017, FEBRUARY, 28, 16, 0)): new CandlestickData(105755L, 109060L, 104950L, 106548L),
        ]
        USDEUR     | ONE_MONTH      | closed(LocalDateTime.of(2017, JANUARY, 31, 16, 0), LocalDateTime.of(2017, FEBRUARY, 28, 16, 0)) | [
                (LocalDateTime.of(2017, JANUARY, 31, 16, 0)) : new CandlestickData(92609, 95294, 92344, 94550),
                (LocalDateTime.of(2017, FEBRUARY, 28, 16, 0)): new CandlestickData(94558, 95283, 91692, 93854),
        ]
    }

    @Unroll
    def 'should limit partial candle data to the current time: #now'() {

        clock.now() >> now

        when:
        def candles = service.getFiveMinuteCandles(EURUSD, closed(LocalDateTime.of(2017, JANUARY, 2, 17, 0), LocalDateTime.of(2017, JANUARY, 2, 17, 5)))

        then:
        candles == expected

        where:
        now                                       | expected
        LocalDateTime.of(2017, JANUARY, 2, 17, 7) | [
                (LocalDateTime.of(2017, JANUARY, 2, 17, 0)): new CandlestickData(104684L, 104711L, 104646L, 104652L),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 5)): new CandlestickData(104656L, 104688L, 104648L, 104650L)
        ]
        LocalDateTime.of(2017, JANUARY, 2, 17, 8) | [
                (LocalDateTime.of(2017, JANUARY, 2, 17, 0)): new CandlestickData(104684L, 104711L, 104646L, 104652L),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 5)): new CandlestickData(104656L, 104688L, 104620L, 104640L)
        ]
    }
}