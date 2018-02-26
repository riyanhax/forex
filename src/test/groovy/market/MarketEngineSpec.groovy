package market

import broker.forex.ForexBroker
import market.forex.ForexMarket
import market.order.OrderRequest
import market.order.OrderStatus
import simulator.SimulatorClock
import simulator.TestClock
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.Month

import static market.forex.Instrument.EURUSD
import static market.order.Orders.buyMarketOrder
import static market.order.Orders.sellMarketOrder

class MarketEngineSpec extends Specification {

    SimulatorClock clock = new TestClock(LocalDateTime.of(2017, Month.JANUARY, 17, 12, 31))
    ForexBroker broker = Mock()
    ForexMarket market = Mock()

    @Unroll
    def 'should be able to submit a #type market order'() {

        def marketPrice = 1.05d
        MarketEngine marketEngine = MarketEngine.create(market, broker, clock)

        when: 'a market order is submitted'
        OrderRequest submittedOrder = marketEngine.submit(order)

        then: 'the order is filled at market price'
        1 * market.getPrice(EURUSD) >> marketPrice

        and: 'the proper response is returned'
        submittedOrder.id
        submittedOrder.status == OrderStatus.OPEN

        def processedOrder = marketEngine.getOrder(submittedOrder)
        and: 'the order is processed'
        processedOrder
        processedOrder.status == OrderStatus.EXECUTED

        and: 'the order was executed at open price'
        processedOrder.executionPrice == marketPrice

        and: 'the broker was notified'
        1 * broker.orderFilled(_ as OrderRequest)

        where:
        type   | order
        'buy'  | buyMarketOrder(10, EURUSD)
        'sell' | sellMarketOrder(10, EURUSD)
    }


}
