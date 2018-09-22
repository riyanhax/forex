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
    def 'should rollup minute data and ranges correctly, timeFrame: #timeFrame, range: #range'() {

        clock.now() >> LocalDateTime.now()

        def actual = service.getOHLC(timeFrame, EURUSD, range)

        expect:
        actual == expected

        where:
        timeFrame      | range                                                                                           | expected
        FIVE_MINUTE    | closed(LocalDateTime.of(2017, JANUARY, 2, 17, 0), LocalDateTime.of(2017, JANUARY, 2, 17, 9))    | [
                (LocalDateTime.of(2017, JANUARY, 2, 17, 0)): new CandlestickData(104684L, 104711L, 104646L, 104652L),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 5)): new CandlestickData(104656L, 104688L, 104608L, 104612L),
        ]
        FIFTEEN_MINUTE | closed(LocalDateTime.of(2017, JANUARY, 2, 17, 0), LocalDateTime.of(2017, JANUARY, 2, 17, 29))   | [
                (LocalDateTime.of(2017, JANUARY, 2, 17, 0)) : new CandlestickData(104684L, 104711L, 104567L, 104570L),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 15)): new CandlestickData(104570L, 104640L, 104568L, 104630L),
        ]
        ONE_HOUR       | closed(LocalDateTime.of(2017, JANUARY, 2, 17, 0), LocalDateTime.of(2017, JANUARY, 2, 18, 0))    | [
                (LocalDateTime.of(2017, JANUARY, 2, 17, 0)): new CandlestickData(104684L, 104711L, 104567L, 104575L),
                (LocalDateTime.of(2017, JANUARY, 2, 18, 0)): new CandlestickData(104577L, 104712L, 104572L, 104662L),
        ]
        FOUR_HOURS     | closed(LocalDateTime.of(2017, JANUARY, 2, 17, 0), LocalDateTime.of(2017, JANUARY, 2, 20, 0))    | [
                // This makes the candle time frame one hour earlier than available data, just like Oanda.
                // Oanda aligns candles from 1700 eastern (so 1600 central)
                (LocalDateTime.of(2017, JANUARY, 2, 16, 0)): new CandlestickData(104684L, 104808L, 104567L, 104758L),
                (LocalDateTime.of(2017, JANUARY, 2, 20, 0)): new CandlestickData(104761L, 104902L, 104709L, 104824L),
        ]
        ONE_DAY        | closed(LocalDateTime.of(2017, JANUARY, 2, 17, 0), LocalDateTime.of(2017, JANUARY, 3, 16, 0))    | [
                (LocalDateTime.of(2017, JANUARY, 2, 16, 0)): new CandlestickData(104684L, 104902L, 103406L, 104050L),
                (LocalDateTime.of(2017, JANUARY, 3, 16, 0)): new CandlestickData(104061L, 105000L, 103899L, 104892L),
        ]
        ONE_WEEK       | closed(LocalDateTime.of(2017, JANUARY, 6, 16, 0), LocalDateTime.of(2017, JANUARY, 13, 16, 0))   | [
                (LocalDateTime.of(2017, JANUARY, 6, 16, 0)) : new CandlestickData(105304L, 106848L, 104539L, 106434L),
                (LocalDateTime.of(2017, JANUARY, 13, 16, 0)): new CandlestickData(106051L, 107194L, 105794L, 107012L),
        ]
        ONE_MONTH      | closed(LocalDateTime.of(2017, JANUARY, 31, 16, 0), LocalDateTime.of(2017, FEBRUARY, 28, 16, 0)) | [
                (LocalDateTime.of(2017, JANUARY, 31, 16, 0)) : new CandlestickData(107980L, 108290L, 104938L, 105764L),
                (LocalDateTime.of(2017, FEBRUARY, 28, 16, 0)): new CandlestickData(105755L, 109060L, 104950L, 106548L),
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