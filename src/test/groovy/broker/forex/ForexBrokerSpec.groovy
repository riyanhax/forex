package broker.forex

import market.MarketEngine
import market.MarketTime
import simulator.HistoryDataForexBroker
import simulator.TestClock
import spock.lang.Specification
import trader.ForexTrader

import static java.time.LocalDateTime.now

class ForexBrokerSpec extends Specification {

    MarketTime clock = new TestClock(now())
    MarketEngine marketEngine = Mock()
    ForexTrader trader = Mock()

    HistoryDataForexBroker broker = new HistoryDataForexBroker(clock, marketEngine, [trader])

//    @Unroll
//    def 'should close existing position when buying/selling the same instrument'() {
//
//        when: 'an existing position is closed'
//        def actual = broker.updatePortfolio(portfolio, order, 1.1d)
//
//        then: 'the portfolio is correct'
//        actual == expected
//
//        where:
//        portfolio                                                             | order                       | expected
//        new ForexPortfolio(500, [new ForexPosition(EURUSD, 10, 1.2d)] as Set) | sellMarketOrder(10, EURUSD) | new ForexPortfolio(511, [] as Set)
//        new ForexPortfolio(500, [new ForexPosition(EURUSD, 10, 1.2d)] as Set) | sellMarketOrder(20, EURUSD) | new ForexPortfolio(511, [new ForexPosition(EURUSD, -10, 1.1d)] as Set)
//        new ForexPortfolio(500, [new ForexPosition(EURUSD, 10, 1.2d)] as Set) | buyMarketOrder(10, EURUSD)  | new ForexPortfolio(489, [new ForexPosition(EURUSD, 20, 1.15d)] as Set)
//    }
}
