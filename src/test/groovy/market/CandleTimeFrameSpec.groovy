package market

import spock.lang.Specification
import spock.lang.Unroll

import static java.time.DayOfWeek.FRIDAY
import static java.time.DayOfWeek.MONDAY
import static java.time.LocalDateTime.of
import static java.time.Month.AUGUST
import static java.time.Month.DECEMBER
import static java.time.Month.FEBRUARY
import static java.time.Month.JANUARY
import static java.time.Month.MARCH
import static java.time.Month.SEPTEMBER
import static market.CandleTimeFrame.FIFTEEN_MINUTE
import static market.CandleTimeFrame.FIVE_MINUTE
import static market.CandleTimeFrame.FOUR_HOURS
import static market.CandleTimeFrame.ONE_DAY
import static market.CandleTimeFrame.ONE_HOUR
import static market.CandleTimeFrame.ONE_MINUTE
import static market.CandleTimeFrame.ONE_MONTH
import static market.CandleTimeFrame.ONE_WEEK
import static market.CandleTimeFrame.THIRTY_MINUTE

class CandleTimeFrameSpec extends Specification {

    @Unroll
    def 'should calculate correct candle starts based on end of trading day hour: #expected, #candle, #dayOfWeekAlignment'() {

        def actual = candle.calculateStart(time, endOfTradingDayHour, dayOfWeekAlignment)

        expect:
        actual == expected

        where:
        candle         | endOfTradingDayHour | dayOfWeekAlignment | time                            | expected
        ONE_MONTH      | 16                  | FRIDAY             | of(2016, FEBRUARY, 29, 15, 59)  | of(2016, JANUARY, 31, 16, 0) // Last minute of trading month
        ONE_MONTH      | 16                  | FRIDAY             | of(2016, FEBRUARY, 1, 0, 0)     | of(2016, JANUARY, 31, 16, 0)
        ONE_MONTH      | 16                  | FRIDAY             | of(2016, JANUARY, 31, 16, 0)    | of(2016, JANUARY, 31, 16, 0)// First minute of trading month
        ONE_MONTH      | 16                  | FRIDAY             | of(2016, JANUARY, 31, 15, 59)   | of(2015, DECEMBER, 31, 16, 0) // Last minute of trading month
        // To verify day of week doesn't affect month
        ONE_MONTH      | 16                  | MONDAY             | of(2016, FEBRUARY, 29, 15, 59)  | of(2016, JANUARY, 31, 16, 0) // Last minute of trading month
        ONE_MONTH      | 16                  | MONDAY             | of(2016, FEBRUARY, 1, 0, 0)     | of(2016, JANUARY, 31, 16, 0)
        ONE_MONTH      | 16                  | MONDAY             | of(2016, JANUARY, 31, 16, 0)    | of(2016, JANUARY, 31, 16, 0)// First minute of trading month
        ONE_MONTH      | 16                  | MONDAY             | of(2016, JANUARY, 31, 15, 59)   | of(2015, DECEMBER, 31, 16, 0) // Last minute of trading month

        ONE_WEEK       | 16                  | FRIDAY             | of(2016, SEPTEMBER, 11, 10, 30) | of(2016, SEPTEMBER, 9, 16, 0)
        ONE_WEEK       | 16                  | FRIDAY             | of(2016, SEPTEMBER, 9, 15, 59)  | of(2016, SEPTEMBER, 2, 16, 0) // Last minute of trading week
        ONE_WEEK       | 16                  | FRIDAY             | of(2016, SEPTEMBER, 9, 16, 0)   | of(2016, SEPTEMBER, 9, 16, 0) // First minute of trading week
        ONE_WEEK       | 16                  | FRIDAY             | of(2016, SEPTEMBER, 10, 4, 0)   | of(2016, SEPTEMBER, 9, 16, 0) // Halfway into trading week
        ONE_WEEK       | 16                  | MONDAY             | of(2016, SEPTEMBER, 7, 10, 30)  | of(2016, SEPTEMBER, 5, 16, 0)
        ONE_WEEK       | 16                  | MONDAY             | of(2016, SEPTEMBER, 4, 15, 59)  | of(2016, AUGUST, 29, 16, 0) // Last minute of trading day
        ONE_WEEK       | 16                  | MONDAY             | of(2016, SEPTEMBER, 5, 16, 0)   | of(2016, SEPTEMBER, 5, 16, 0) // First minute of trading day
        ONE_WEEK       | 16                  | MONDAY             | of(2016, SEPTEMBER, 6, 4, 0)    | of(2016, SEPTEMBER, 5, 16, 0) // Halfway into trading day

        ONE_DAY        | 4                   | FRIDAY             | of(2017, JANUARY, 3, 3, 59)     | of(2017, JANUARY, 2, 4, 0) // Last minute of trading day
        ONE_DAY        | 4                   | FRIDAY             | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 3, 4, 0) // First minute of trading day
        ONE_DAY        | 16                  | FRIDAY             | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 2, 16, 0) // Halfway into trading day
        ONE_DAY        | 17                  | FRIDAY             | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 2, 17, 0) // Halfway into trading day
        // To verify day of week doesn't affect day
        ONE_DAY        | 4                   | MONDAY             | of(2017, JANUARY, 3, 3, 59)     | of(2017, JANUARY, 2, 4, 0) // Last minute of trading day
        ONE_DAY        | 4                   | MONDAY             | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 3, 4, 0) // First minute of trading day
        ONE_DAY        | 16                  | MONDAY             | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 2, 16, 0) // Halfway into trading day
        ONE_DAY        | 17                  | MONDAY             | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 2, 17, 0) // Halfway into trading day

        FOUR_HOURS     | 15                  | FRIDAY             | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 3, 3, 0)
        FOUR_HOURS     | 16                  | FRIDAY             | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 3, 4, 0)
        FOUR_HOURS     | 17                  | FRIDAY             | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 3, 1, 0)
        // To verify day of week doesn't affect four hour
        FOUR_HOURS     | 15                  | MONDAY             | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 3, 3, 0)
        FOUR_HOURS     | 16                  | MONDAY             | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 3, 4, 0)
        FOUR_HOURS     | 17                  | MONDAY             | of(2017, JANUARY, 3, 4, 0)      | of(2017, JANUARY, 3, 1, 0)

        // Day of week and alignment hour are tested to make sure they don't affect these time frames
        ONE_HOUR       | 15                  | FRIDAY             | of(2017, JANUARY, 3, 4, 59)     | of(2017, JANUARY, 3, 4, 0)// Last minute of trading hour
        ONE_HOUR       | 16                  | FRIDAY             | of(2017, JANUARY, 3, 5, 0)      | of(2017, JANUARY, 3, 5, 0)// First minute of trading hour
        ONE_HOUR       | 17                  | FRIDAY             | of(2017, JANUARY, 3, 3, 30)     | of(2017, JANUARY, 3, 3, 0)// Halfway into trading hour
        ONE_HOUR       | 15                  | MONDAY             | of(2017, JANUARY, 3, 4, 59)     | of(2017, JANUARY, 3, 4, 0)// Last minute of trading hour
        ONE_HOUR       | 16                  | MONDAY             | of(2017, JANUARY, 3, 5, 0)      | of(2017, JANUARY, 3, 5, 0)// First minute of trading hour
        ONE_HOUR       | 17                  | MONDAY             | of(2017, JANUARY, 3, 3, 30)     | of(2017, JANUARY, 3, 3, 0)// Halfway into trading hour

        THIRTY_MINUTE  | 15                  | FRIDAY             | of(2017, JANUARY, 3, 4, 59)     | of(2017, JANUARY, 3, 4, 30)// Last minute of trading 30m
        THIRTY_MINUTE  | 16                  | FRIDAY             | of(2017, JANUARY, 3, 5, 0)      | of(2017, JANUARY, 3, 5, 0)// First minute of trading 30m
        THIRTY_MINUTE  | 17                  | FRIDAY             | of(2017, JANUARY, 3, 3, 15)     | of(2017, JANUARY, 3, 3, 0)// Halfway into trading 30m
        THIRTY_MINUTE  | 15                  | MONDAY             | of(2017, JANUARY, 3, 4, 59)     | of(2017, JANUARY, 3, 4, 30)// Last minute of trading 30m
        THIRTY_MINUTE  | 16                  | MONDAY             | of(2017, JANUARY, 3, 5, 0)      | of(2017, JANUARY, 3, 5, 0)// First minute of trading 30m
        THIRTY_MINUTE  | 17                  | MONDAY             | of(2017, JANUARY, 3, 3, 15)     | of(2017, JANUARY, 3, 3, 0)// Halfway into trading 30m

        FIFTEEN_MINUTE | 15                  | FRIDAY             | of(2017, JANUARY, 3, 4, 59)     | of(2017, JANUARY, 3, 4, 45)// Last minute of trading 15m
        FIFTEEN_MINUTE | 16                  | FRIDAY             | of(2017, JANUARY, 3, 5, 0)      | of(2017, JANUARY, 3, 5, 0)// First minute of trading 15m
        FIFTEEN_MINUTE | 17                  | FRIDAY             | of(2017, JANUARY, 3, 3, 7)      | of(2017, JANUARY, 3, 3, 0)// Halfway into trading 15m
        FIFTEEN_MINUTE | 15                  | MONDAY             | of(2017, JANUARY, 3, 4, 59)     | of(2017, JANUARY, 3, 4, 45)// Last minute of trading 15m
        FIFTEEN_MINUTE | 16                  | MONDAY             | of(2017, JANUARY, 3, 5, 0)      | of(2017, JANUARY, 3, 5, 0)// First minute of trading 15m
        FIFTEEN_MINUTE | 17                  | MONDAY             | of(2017, JANUARY, 3, 3, 7)      | of(2017, JANUARY, 3, 3, 0)// Halfway into trading 15m

        FIVE_MINUTE    | 15                  | FRIDAY             | of(2017, JANUARY, 3, 4, 59)     | of(2017, JANUARY, 3, 4, 55)// Last minute of trading 5m
        FIVE_MINUTE    | 16                  | FRIDAY             | of(2017, JANUARY, 3, 5, 0)      | of(2017, JANUARY, 3, 5, 0)// First minute of trading 5m
        FIVE_MINUTE    | 17                  | FRIDAY             | of(2017, JANUARY, 3, 3, 7)      | of(2017, JANUARY, 3, 3, 5)// Halfway into trading 5m
        FIVE_MINUTE    | 15                  | MONDAY             | of(2017, JANUARY, 3, 4, 59)     | of(2017, JANUARY, 3, 4, 55)// Last minute of trading 5m
        FIVE_MINUTE    | 16                  | MONDAY             | of(2017, JANUARY, 3, 5, 0)      | of(2017, JANUARY, 3, 5, 0)// First minute of trading 5m
        FIVE_MINUTE    | 17                  | MONDAY             | of(2017, JANUARY, 3, 3, 7)      | of(2017, JANUARY, 3, 3, 5)// Halfway into trading 5m

        ONE_MINUTE     | 15                  | FRIDAY             | of(2017, JANUARY, 3, 4, 59, 59) | of(2017, JANUARY, 3, 4, 59)// Last 30s of trading 1m
        ONE_MINUTE     | 16                  | FRIDAY             | of(2017, JANUARY, 3, 5, 0, 0)   | of(2017, JANUARY, 3, 5, 0)// First 30s of trading 1m
        ONE_MINUTE     | 17                  | FRIDAY             | of(2017, JANUARY, 3, 3, 7, 30)  | of(2017, JANUARY, 3, 3, 7)// Halfway into trading 1m
        ONE_MINUTE     | 15                  | MONDAY             | of(2017, JANUARY, 3, 4, 59)     | of(2017, JANUARY, 3, 4, 59)// Last 30s of trading 1m
        ONE_MINUTE     | 16                  | MONDAY             | of(2017, JANUARY, 3, 5, 0)      | of(2017, JANUARY, 3, 5, 0)// First 30s of trading 1m
        ONE_MINUTE     | 17                  | MONDAY             | of(2017, JANUARY, 3, 3, 7)      | of(2017, JANUARY, 3, 3, 7)// Halfway into trading 1m
    }

    @Unroll
    def 'should calculate next trading candle start correctly: #timeFrame, #time, #expected'() {

        def actual = timeFrame.nextCandle(time)

        expect:
        actual == expected

        where:
        timeFrame      | time                             | expected
        ONE_MONTH      | of(2016, FEBRUARY, 29, 15, 59)   | of(2016, FEBRUARY, 29, 16, 0) // Last minute of trading month
        ONE_MONTH      | of(2016, FEBRUARY, 29, 16, 0)    | of(2016, MARCH, 31, 16, 0)// First minute of trading month
        ONE_MONTH      | of(2016, MARCH, 15, 13, 0)       | of(2016, MARCH, 31, 16, 0)// Middle of trading month

        ONE_WEEK       | of(2016, SEPTEMBER, 9, 15, 59)   | of(2016, SEPTEMBER, 9, 16, 0)// Last minute of trading week
        ONE_WEEK       | of(2016, SEPTEMBER, 9, 16, 0)    | of(2016, SEPTEMBER, 16, 16, 0) // First minute of trading week
        ONE_WEEK       | of(2016, SEPTEMBER, 11, 10, 30)  | of(2016, SEPTEMBER, 16, 16, 0) // Middle of trading week

        ONE_DAY        | of(2016, JANUARY, 3, 15, 59)     | of(2016, JANUARY, 3, 16, 0)// Last minute of trading day
        ONE_DAY        | of(2016, JANUARY, 3, 16, 0)      | of(2016, JANUARY, 4, 16, 0)// First minute of trading day
        ONE_DAY        | of(2016, JANUARY, 4, 4, 0)       | of(2016, JANUARY, 4, 16, 0)// Halfway into trading day

        FOUR_HOURS     | of(2016, JANUARY, 3, 15, 59)     | of(2016, JANUARY, 3, 16, 0)// Last minute of trading 4h
        FOUR_HOURS     | of(2016, JANUARY, 3, 16, 0)      | of(2016, JANUARY, 3, 20, 0)// First minute of trading 4h
        FOUR_HOURS     | of(2016, JANUARY, 3, 18, 0)      | of(2016, JANUARY, 3, 20, 0)// Halfway into trading 4h

        ONE_HOUR       | of(2016, JANUARY, 3, 15, 59)     | of(2016, JANUARY, 3, 16, 0)// Last minute of trading hour
        ONE_HOUR       | of(2016, JANUARY, 3, 16, 0)      | of(2016, JANUARY, 3, 17, 0)// First minute of trading hour
        ONE_HOUR       | of(2016, JANUARY, 3, 16, 30)     | of(2016, JANUARY, 3, 17, 0)// Halfway into trading hour

        THIRTY_MINUTE  | of(2016, JANUARY, 3, 15, 59)     | of(2016, JANUARY, 3, 16, 0)// Last minute of trading 30m
        THIRTY_MINUTE  | of(2016, JANUARY, 3, 16, 0)      | of(2016, JANUARY, 3, 16, 30)// First minute of trading 30m
        THIRTY_MINUTE  | of(2016, JANUARY, 3, 16, 45)     | of(2016, JANUARY, 3, 17, 0)// Halfway into trading 30m

        FIFTEEN_MINUTE | of(2016, JANUARY, 3, 15, 59)     | of(2016, JANUARY, 3, 16, 0)// Last minute of trading 15m
        FIFTEEN_MINUTE | of(2016, JANUARY, 3, 16, 0)      | of(2016, JANUARY, 3, 16, 15)// First minute of trading 15m
        FIFTEEN_MINUTE | of(2016, JANUARY, 3, 16, 49)     | of(2016, JANUARY, 3, 17, 0)// Halfway into trading 15m

        FIVE_MINUTE    | of(2016, JANUARY, 3, 15, 59)     | of(2016, JANUARY, 3, 16, 0)// Last minute of trading 5m
        FIVE_MINUTE    | of(2016, JANUARY, 3, 16, 0)      | of(2016, JANUARY, 3, 16, 5)// First minute of trading 5m
        FIVE_MINUTE    | of(2016, JANUARY, 3, 16, 57)     | of(2016, JANUARY, 3, 17, 0)// Halfway into trading 5m

        ONE_MINUTE     | of(2016, JANUARY, 3, 15, 59, 59) | of(2016, JANUARY, 3, 16, 0, 0)// Last 30s of trading 1m
        ONE_MINUTE     | of(2016, JANUARY, 3, 16, 0, 0)   | of(2016, JANUARY, 3, 16, 1, 0)// First 30s of trading 1m
        ONE_MINUTE     | of(2016, JANUARY, 3, 16, 57, 30) | of(2016, JANUARY, 3, 16, 58, 0)// Halfway into trading 1m
    }
}
