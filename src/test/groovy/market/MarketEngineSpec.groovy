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
import static market.order.Orders.*

class MarketEngineSpec extends Specification {

    SimulatorClock clock = new TestClock(LocalDateTime.of(2017, Month.JANUARY, 17, 12, 31))
    ForexBroker broker = Mock()
    ForexMarket market = Mock()

    @Unroll
    def 'should be able to submit a #type market order'() {

        def marketPrice = 1.05d
        MarketEngine marketEngine = MarketEngine.create(market, clock)

        when: 'a market order is submitted'
        OrderRequest submittedOrder = marketEngine.submit(broker, order)

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
        processedOrder.executionPrice.get() == marketPrice

        and: 'the broker was notified'
        1 * broker.orderFilled(_ as OrderRequest)

        where:
        type   | order
        'buy'  | buyMarketOrder(10, EURUSD)
        'sell' | sellMarketOrder(10, EURUSD)
    }

    @Unroll
    def 'should be able to submit a #type limit order'() {

        def marketPrice = 1.05d
        MarketEngine marketEngine = MarketEngine.create(market, clock)

        when: 'a limit order is submitted'
        OrderRequest submittedOrder = marketEngine.submit(broker, order)

        then: 'the order cannot be filled at market price'
        1 * market.getPrice(EURUSD) >> marketPrice

        and: 'the proper response is returned'
        submittedOrder.id
        submittedOrder.status == OrderStatus.OPEN

        def processedOrder = marketEngine.getOrder(submittedOrder)
        and: 'the order status is correct'
        processedOrder
        processedOrder.status == expectedStatus

        and: 'the order was not executed at open price'
        processedOrder.executionPrice == expectedExecutionPrice

        and: 'the broker was not notified'
        (expectedExecutionPrice.isPresent() ? 1 : 0) * broker.orderFilled(_ as OrderRequest)

        where:
        type                         | order                             | expectedStatus       | expectedExecutionPrice
        'buy (limit not satisfied)'  | buyLimitOrder(10, EURUSD, 1.0d)   | OrderStatus.OPEN     | Optional.empty()
        'sell (limit not satisfied)' | sellLimitOrder(10, EURUSD, 1.10d) | OrderStatus.OPEN     | Optional.empty()
        'buy (limit satisfied)'      | buyLimitOrder(10, EURUSD, 1.10d)  | OrderStatus.EXECUTED | Optional.of(1.05d)
        'sell (limit satisfied)'     | sellLimitOrder(10, EURUSD, 1.0d)  | OrderStatus.EXECUTED | Optional.of(1.05d)
    }
}
