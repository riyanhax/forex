package simulator

import broker.CandlestickData
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

import static com.google.common.collect.Range.closed
import static java.time.Month.FEBRUARY
import static java.time.Month.JANUARY
import static market.CandleTimeFrame.FIFTEEN_MINUTE
import static market.CandleTimeFrame.FIVE_MINUTE
import static market.CandleTimeFrame.FOUR_HOURS
import static market.CandleTimeFrame.ONE_DAY
import static market.CandleTimeFrame.ONE_HOUR
import static market.CandleTimeFrame.ONE_MONTH
import static market.CandleTimeFrame.ONE_WEEK
import static market.Instrument.EURUSD

class HistoryDataServiceSpec extends Specification {

    static HistoryDataService service = new HistoryDataService(new TestClock(LocalDateTime.now()), '/history/Oanda_%s_%d.csv')

    def 'should read history file as UTC-5 minute data, convert to local, and treat data range inclusive'() {

        def firstSixMinutes = service.getOneMinuteCandles(EURUSD, closed(
                LocalDateTime.of(2017, JANUARY, 2, 17, 0),
                LocalDateTime.of(2017, JANUARY, 2, 17, 5)
        ))

        expect:
        firstSixMinutes == [
                (LocalDateTime.of(2017, JANUARY, 2, 17, 0)): new CandlestickData(104684L, 104687L, 104662L, 104680L),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 1)): new CandlestickData(104680L, 104707L, 104675L, 104688L),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 2)): new CandlestickData(104690L, 104711L, 104674L, 104674L),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 3)): new CandlestickData(104670L, 104680L, 104654L, 104680L),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 4)): new CandlestickData(104674L, 104674L, 104646L, 104652L),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 5)): new CandlestickData(104656L, 104688L, 104656L, 104671L)
        ]
    }

    @Unroll
    def 'should rollup minute data and ranges correctly, timeFrame: #timeFrame, range: #range'() {

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

}