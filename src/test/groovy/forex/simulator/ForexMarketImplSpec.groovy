package forex.simulator

import forex.broker.CandlestickData
import forex.market.InstrumentHistory
import forex.market.InstrumentHistoryService
import spock.lang.Specification

import java.time.LocalDateTime

import static forex.market.Instrument.EURUSD

class ForexMarketImplSpec extends Specification {

    static clock = new TestClock(LocalDateTime.now())

    def historyService = Mock(InstrumentHistoryService)

    def 'should use the open price from the history data service for price requests'() {
        def openPrice = 10L

        def market = new ForexMarketImpl(clock, historyService)

        when: 'a price is requested'
        def actual = market.getPrice(EURUSD)

        then: 'the data was retrieved from the history data service'
        1 * historyService.getData(EURUSD, clock.now()) >> Optional.of(new InstrumentHistory(
                EURUSD, clock.now(), new CandlestickData(openPrice, 20L, 5L, 7L)
        ))

        and: 'the open price was used'
        actual == openPrice
    }

    def 'should base whether the market is available on whether history data is available for the current time'() {
        def market = new ForexMarketImpl(clock, historyService)
        def expected = data != null

        when: 'is available is checked'
        def actual = market.isAvailable()

        then: 'history data was retrieved'
        1 * historyService.getData(EURUSD, clock.now()) >> Optional.ofNullable(data)

        and: 'it returns true if data was available'
        actual == expected

        where:
        data << [
                null,
                new InstrumentHistory(EURUSD, clock.now(), new CandlestickData(10L, 20L, 5L, 7L))
        ]
    }

}
