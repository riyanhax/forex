package market

import spock.lang.Specification
import spock.lang.Unroll

import static java.time.DayOfWeek.FRIDAY
import static java.time.LocalDateTime.of
import static java.time.Month.JANUARY
import static java.time.Month.SEPTEMBER
import static market.CandleTimeFrame.FOUR_HOURS
import static market.CandleTimeFrame.ONE_DAY
import static market.CandleTimeFrame.ONE_WEEK

class CandleTimeFrameSpec extends Specification {

    @Unroll
    def 'should calculate correct candle starts based on end of trading day hour: #expected'() {

        // TODO: Add other days of week for weekly alignment testing
        def actual = candle.calculateStart(time, endOfTradingDayHour, FRIDAY)

        expect:
        actual == expected

        where:
        candle     | endOfTradingDayHour | time                            | expected
        // TODO: Month tests!

        ONE_WEEK   | 16                  | of(2016, SEPTEMBER, 11, 10, 30) | of(2016, SEPTEMBER, 9, 16, 0)
        ONE_WEEK   | 16                  | of(2016, SEPTEMBER, 9, 15, 59)  | of(2016, SEPTEMBER, 2, 16, 0) // Last minute of trading day
        ONE_WEEK   | 16                  | of(2016, SEPTEMBER, 9, 16, 0)   | of(2016, SEPTEMBER, 9, 16, 0) // First minute of trading day
        ONE_WEEK   | 16                  | of(2016, SEPTEMBER, 10, 4, 0)   | of(2016, SEPTEMBER, 9, 16, 0) // Halfway into trading day

        ONE_DAY    | 4                   | of(2017, JANUARY, 3, 3, 59)     | of(2017, JANUARY, 2, 4, 0) // Last minute of trading day
        ONE_DAY    | 4                   | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 3, 4, 0) // First minute of trading day
        ONE_DAY    | 16                  | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 2, 16, 0) // Halfway into trading day
        ONE_DAY    | 17                  | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 2, 17, 0) // Halfway into trading day

        FOUR_HOURS | 15                  | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 3, 3, 0)
        FOUR_HOURS | 16                  | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 3, 4, 0)
        FOUR_HOURS | 17                  | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 3, 1, 0)
    }
}
