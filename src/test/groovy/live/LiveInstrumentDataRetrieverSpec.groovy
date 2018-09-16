package live


import com.google.common.collect.Range
import live.LiveInstrumentDataRetriever
import market.InstrumentCandleService
import market.MarketTime
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.Month
import java.util.function.Function

import static com.google.common.collect.Range.closed
import static java.time.LocalDateTime.of as ldt

class LiveInstrumentDataRetrieverSpec extends Specification {

    def clock = Mock(MarketTime)
    def instrumentCandleService = Mock(InstrumentCandleService)

    @Unroll
    def 'should process each range for candle retrievals'() {

        def firstRange = closed(ldt(2018, Month.SEPTEMBER, 10, 8, 30), ldt(2018, Month.SEPTEMBER, 13, 19, 50))
        def secondRange = closed(ldt(2018, Month.SEPTEMBER, 13, 19, 51), ldt(2018, Month.SEPTEMBER, 14, 8, 31))

        def retrievalRanges = new TreeSet<Range<LocalDateTime>>(Comparator.comparing(new Function<Range, LocalDateTime>() {
            @Override
            LocalDateTime apply(Range range) {
                return range.lowerEndpoint()
            }
        }))
        retrievalRanges.add(firstRange)
        retrievalRanges.add(secondRange)

        def actual = new LiveInstrumentDataRetriever(clock, instrumentCandleService) {
            @Override
            SortedSet<Range<LocalDateTime>> determineRetrievalRanges() {
                return retrievalRanges
            }
        }

        when: 'closed candles are retrieved'
        actual.retrieveClosedCandles()

        then: 'the first range is retrieved'
        1 * instrumentCandleService.retrieveAndStoreOneMinuteCandles(firstRange) >> []

        then: 'the second range is retrieved'
        1 * instrumentCandleService.retrieveAndStoreOneMinuteCandles(secondRange) >> []
    }

    @Unroll
    def 'should batch any deltas from the latest stored candle to most recent completed minute into ranges of 2500 minutes: #expected'() {

        clock.now() >> now

        def latestStoredMinute = latestStored
        def actual = new LiveInstrumentDataRetriever(clock, instrumentCandleService) {
            @Override
            LocalDateTime findLatestStoredMinute() {
                return latestStoredMinute
            }
        }.determineRetrievalRanges()

        expect:
        actual == expected as Set

        where:
        latestStored                          | now                                       | expected
        ldt(2018, Month.SEPTEMBER, 14, 8, 30) | ldt(2018, Month.SEPTEMBER, 14, 8, 31)     | []
        ldt(2018, Month.SEPTEMBER, 14, 8, 29) | ldt(2018, Month.SEPTEMBER, 14, 8, 31)     | [closed(ldt(2018, Month.SEPTEMBER, 14, 8, 30), ldt(2018, Month.SEPTEMBER, 14, 8, 30))]
        ldt(2018, Month.SEPTEMBER, 14, 8, 29) | ldt(2018, Month.SEPTEMBER, 14, 8, 31, 59) | [closed(ldt(2018, Month.SEPTEMBER, 14, 8, 30), ldt(2018, Month.SEPTEMBER, 14, 8, 30))]
        ldt(2018, Month.SEPTEMBER, 14, 8, 29) | ldt(2018, Month.SEPTEMBER, 14, 8, 32)     | [closed(ldt(2018, Month.SEPTEMBER, 14, 8, 30), ldt(2018, Month.SEPTEMBER, 14, 8, 31))]
        ldt(2018, Month.SEPTEMBER, 10, 8, 29) | ldt(2018, Month.SEPTEMBER, 14, 8, 32)     | [closed(ldt(2018, Month.SEPTEMBER, 10, 8, 30), ldt(2018, Month.SEPTEMBER, 12, 2, 10)),
                                                                                             closed(ldt(2018, Month.SEPTEMBER, 12, 2, 11), ldt(2018, Month.SEPTEMBER, 13, 19, 51)),
                                                                                             closed(ldt(2018, Month.SEPTEMBER, 13, 19, 52), ldt(2018, Month.SEPTEMBER, 14, 8, 31))]
    }

}
