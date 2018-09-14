package market

import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.Month

import static com.google.common.collect.Range.closed
import static java.time.LocalDateTime.of as ldt

class DataRetrieverSpec extends Specification {

    @Unroll
    def 'should batch any deltas from the latest stored candle to most recent completed minute into ranges of 5000 minutes: #expected'() {

        def clock = Mock(MarketTime)
        clock.now() >> now

        def latestStoredMinute = latestStored
        def actual = new DataRetriever(clock) {
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
        ldt(2018, Month.SEPTEMBER, 10, 8, 29) | ldt(2018, Month.SEPTEMBER, 14, 8, 32)     | [closed(ldt(2018, Month.SEPTEMBER, 10, 8, 30), ldt(2018, Month.SEPTEMBER, 13, 19, 50)),
                                                                                             closed(ldt(2018, Month.SEPTEMBER, 13, 19, 51), ldt(2018, Month.SEPTEMBER, 14, 8, 31))]
    }
}
