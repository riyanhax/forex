package live

import broker.AccountContext
import broker.AccountGetResponse
import broker.Context
import broker.MarketOrderRequest
import broker.OpenPositionRequest
import broker.OrderContext
import broker.OrderCreateResponse
import broker.Price
import broker.PricingContext
import broker.PricingGetResponse
import broker.Quote
import broker.StopLossDetails
import broker.TakeProfitDetails
import market.MarketTime
import spock.lang.Specification
import spock.lang.Unroll

import static market.Instrument.EURUSD
import static market.Instrument.USDEUR

class OandaSpec extends Specification {

    @Unroll
    // Can't figure out why I have to set these weird expected stop loss and take profits
    def 'inverse instruments should use ask price and the strange stop loss and take profit: #instrument'() {
        Quote quote = Mock(Quote)
        quote.ask >> ask
        quote.bid >> bid

        def actual = Oanda.createMarketOrderRequest(quote, instrument, 2, 300L, 600L)

        expect:
        actual == new MarketOrderRequest(instrument: instrument, units: 2,
                stopLossOnFill: new StopLossDetails(price: expectedStopLoss),
                takeProfitOnFill: new TakeProfitDetails(price: expectedTakeProfit))

        where:
        instrument | bid     | ask     | expectedStopLoss | expectedTakeProfit
        EURUSD     | 123060L | 123106L | 122760L          | 123660
        USDEUR     | 81235L  | 81265L  | 81067L           | 81663L
    }

    def 'should use the specified units from the request'() {
        def context = Mock(Context)

        def accountContext = Mock(AccountContext)
        context.account() >> accountContext
        accountContext.get(_) >> new AccountGetResponse()

        def orderContext = Mock(OrderContext)
        context.order() >> orderContext

        def pricingContext = Mock(PricingContext)
        context.pricing() >> pricingContext
        pricingContext.get(_) >> new PricingGetResponse([new Price(EURUSD, 10010L, 10020L)])

        def clock = Mock(MarketTime)
        def trader = new MockOandaTrader(context, clock)

        Oanda oanda = new Oanda(clock, new LiveTraders([trader]))

        when: 'a trader opens a position with a specific number of units'
        oanda.openPosition(trader, new OpenPositionRequest(EURUSD, 3, null, null, null))

        then: 'the position is opened with the requested number of units'
        1 * orderContext.create({ it.order.units == 3 }) >> new OrderCreateResponse()
    }
}
