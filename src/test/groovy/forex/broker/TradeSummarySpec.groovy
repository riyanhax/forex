package forex.broker

import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.Month

import static forex.market.Instrument.USDEUR

class TradeSummarySpec extends Specification {

    @Unroll
    def 'should calculate current price based on realized P&L only when the trade is closed'() {

        def currentUnits = closeTime == null ? units : 0
        def actual = new TradeSummary('309', '1', USDEUR, 86233L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 7, 43, 13, 567036542),
                units, currentUnits, realizedPL, unrealizedPL, closeTime)
                .currentPrice

        expect:
        actual == expected

        where:
        expected | unrealizedPL | realizedPL | units | price  | closeTime
        86333L   | 0L           | 100L       | 1     | 86233L | LocalDateTime.of(2018, Month.SEPTEMBER, 7, 07, 45, 11, 338759441)
        86333L   | 10L          | 100L       | 1     | 86233L | LocalDateTime.of(2018, Month.SEPTEMBER, 7, 07, 45, 11, 338759441)
        86243L   | 10L          | 0L         | 1     | 86233L | null
        86243L   | 10L          | 100L       | 1     | 86233L | null
        86283L   | 10L          | 100L       | 2     | 86233L | LocalDateTime.of(2018, Month.SEPTEMBER, 7, 07, 45, 11, 338759441)
        86238L   | 10L          | 0L         | 2     | 86233L | null
    }

    @Unroll
    def 'should calculate net asset value based on price and unrealized/realized P&L: @expected'() {

        def currentUnits = closeTime == null ? units : 0
        def actual = new TradeSummary('309', '1', USDEUR, 86233L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 7, 43, 13, 567036542),
                units, currentUnits, realizedPL, unrealizedPL, closeTime)
                .netAssetValue

        expect:
        actual == expected

        where:
        expected | unrealizedPL | realizedPL | units | price  | closeTime
        86333L   | 0L           | 100L       | 1     | 86233L | LocalDateTime.of(2018, Month.SEPTEMBER, 7, 07, 45, 11, 338759441)
        86333L   | 10L          | 100L       | 1     | 86233L | LocalDateTime.of(2018, Month.SEPTEMBER, 7, 07, 45, 11, 338759441)
        86243L   | 10L          | 0L         | 1     | 86233L | null
        86243L   | 10L          | 100L       | 1     | 86233L | null
        172566L  | 10L          | 100L       | 2     | 86233L | LocalDateTime.of(2018, Month.SEPTEMBER, 7, 07, 45, 11, 338759441)
        172476L  | 10L          | 0L         | 2     | 86233L | null
    }
}
