package market

import market.order.OrderRequest
import market.order.OrderStatus
import simulator.TestClock
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.Month

import static Instrument.EURUSD
import static market.order.Orders.buyLimitOrder
import static market.order.Orders.buyMarketOrder
import static market.order.Orders.sellLimitOrder
import static market.order.Orders.sellMarketOrder

class MarketEngineSpec extends Specification {

    static MarketTime clock = new TestClock(LocalDateTime.of(2017, Month.JANUARY, 17, 12, 31))
    OrderListener broker = Mock()
    ForexMarket market = Mock()

    @Unroll
    def 'should be able to submit a #type market order'() {

        def marketPrice = 105000d
        MarketEngine marketEngine = MarketEngine.create(market, clock)

        when: 'a market order is submitted'
        OrderRequest submittedOrder = marketEngine.submit(broker, order)

        and: 'orders are processed'
        marketEngine.processUpdates()

        then: 'the order is filled at market price'
        1 * market.isAvailable() >> true
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

        def marketPrice = 105000d
        MarketEngine marketEngine = MarketEngine.create(market, clock)

        when: 'a limit order is submitted'
        OrderRequest submittedOrder = marketEngine.submit(broker, order)

        and: 'orders are processed'
        marketEngine.processUpdates()

        then: 'the order cannot be filled at market price'
        1 * market.isAvailable() >> true
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
        type                         | order                               | expectedStatus       | expectedExecutionPrice
        'buy (limit not satisfied)'  | buyLimitOrder(10, EURUSD, 100000L)  | OrderStatus.OPEN     | Optional.empty()
        'sell (limit not satisfied)' | sellLimitOrder(10, EURUSD, 110000L) | OrderStatus.OPEN     | Optional.empty()
        'buy (limit satisfied)'      | buyLimitOrder(10, EURUSD, 110000L)  | OrderStatus.EXECUTED | Optional.of(105000L)
        'sell (limit satisfied)'     | sellLimitOrder(10, EURUSD, 100000L) | OrderStatus.EXECUTED | Optional.of(105000L)
    }

    def 'should not process orders when the market is closed'() {

        MarketEngine marketEngine = MarketEngine.create(market, clock)

        given: 'a limit order was previously submitted'
        OrderRequest submittedOrder = marketEngine.submit(broker, buyLimitOrder(10, EURUSD, 0L))

        when: 'updates are ran'
        marketEngine.processUpdates()

        then: 'it was seen whether tha market was available'
        1 * market.isAvailable() >> false

        and: 'the price is not retrieved'
        0 * market.getPrice(submittedOrder.instrument)

        and: 'the order remains open'
        marketEngine.getOrder(submittedOrder).status == OrderStatus.OPEN
    }

    @Unroll
    def 'should cancel unfilled expired orders: #expiration'() {
        MarketEngine marketEngine = MarketEngine.create(market, clock)
        market.isAvailable() >> true

        given: 'a limit order was previously submitted'
        OrderRequest submittedOrder = marketEngine.submit(broker, buyLimitOrder(5, EURUSD, 10L, expiration))

        when: 'updates are ran'
        marketEngine.processUpdates()

        then: 'the expired order is cancelled'
        1 * broker.orderCancelled({ it.id == submittedOrder.id })

        where:
        expiration << [
                clock.now().minusHours(1), clock.now().minusMinutes(1), clock.now()
        ]
    }

    @Unroll
    def 'should not cancel unfilled unexpired orders: #expiration'() {
        MarketEngine marketEngine = MarketEngine.create(market, clock)
        market.isAvailable() >> true

        given: 'a limit order was previously submitted'
        marketEngine.submit(broker, buyLimitOrder(5, EURUSD, 10L, expiration))

        when: 'updates are ran'
        marketEngine.processUpdates()

        then: 'the expired order is cancelled'
        0 * broker.orderCancelled(_ as OrderRequest)

        where:
        expiration << [
                clock.now().plusHours(1), clock.now().plusMinutes(1)
        ]
    }
}
