package market.order

import spock.lang.Specification
import spock.lang.Unroll

import static market.Instrument.EURUSD

class OrdersSpec extends Specification {

    @Unroll
    def 'should be able to create #type market orders'() {

        expect: 'it has the correct attributes'
        expectedType.isAssignableFrom(order.class)
        order.units == 10
        order.instrument == EURUSD

        where:
        type   | order                              | expectedType
        'buy'  | Orders.buyMarketOrder(10, EURUSD)  | BuyMarketOrder
        'sell' | Orders.sellMarketOrder(10, EURUSD) | SellMarketOrder
    }

    @Unroll
    def 'should be able to create #type limit orders'() {

        expect: 'it has the correct attributes'
        expectedType.isAssignableFrom(order.class)
        order.units == 10
        order.instrument == EURUSD
        order.limit() == Optional.of(2.550d)

        where:
        type   | order                                     | expectedType
        'buy'  | Orders.buyLimitOrder(10, EURUSD, 2.550d)  | BuyLimitOrder
        'sell' | Orders.sellLimitOrder(10, EURUSD, 2.550d) | SellLimitOrder
    }

//    @Unroll
//    def 'should be able to create #type stop orders'() {
//
//        expect: 'it has the correct attributes'
//        expectedType.isAssignableFrom(order.class)
//        order.shares == 10
//        order.security == security
//        order.stopPrice == 2550L
//        order.duration == Duration.GTC
//
//        where:
//        type   | order                                                      | expectedType
//        'buy'  | Orders.newBuyStopOrder(10, security, 2550L, Duration.GTC)  | BuyStopOrder
//        'sell' | Orders.newSellStopOrder(10, security, 2550L, Duration.GTC) | SellStopOrder
//    }
//
//    @Unroll
//    def 'should be able to create #type stop limit orders'() {
//
//        expect: 'it has the correct attributes'
//        expectedType.isAssignableFrom(order.class)
//        order.shares == 10
//        order.security == security
//        order.stopPrice == 2550L
//        order.price == 2950L
//        order.duration == Duration.GTC
//
//        where:
//        type   | order                                                                  | expectedType
//        'buy'  | Orders.newBuyStopLimitOrder(10, security, 2550L, 2950L, Duration.GTC)  | BuyStopLimitOrder
//        'sell' | Orders.newSellStopLimitOrder(10, security, 2550L, 2950L, Duration.GTC) | SellStopLimitOrder
//    }
//
//    @Unroll
//    def 'should be able to create #type market on close orders'() {
//
//        expect: 'it has the correct attributes'
//        expectedType.isAssignableFrom(order.class)
//        order.shares == 10
//        order.security == security
//
//        where:
//        type   | order                                          | expectedType
//        'buy'  | Orders.newBuyMarketOnCloseOrder(10, security)  | BuyMarketOnCloseOrder
//        'sell' | Orders.newSellMarketOnCloseOrder(10, security) | SellMarketOnCloseOrder
//    }
//
//    @Unroll
//    def 'should be able to create one cancel other orders'() {
//
//        expect: 'it has the correct attributes'
//        OneCancelsOther oco = Orders.newOneCancelsOtherOrder(firstOrder, secondOrder)
//        oco.firstOrder.is(firstOrder)
//        oco.secondOrder.is(secondOrder)
//
//        where:
//        firstOrder                                     | secondOrder
//        Orders.newBuyMarketOnCloseOrder(10, security)  | Orders.newSellMarketOnCloseOrder(10, security)
//        Orders.newSellMarketOnCloseOrder(10, security) | Orders.newBuyMarketOnCloseOrder(10, security)
//    }
}
