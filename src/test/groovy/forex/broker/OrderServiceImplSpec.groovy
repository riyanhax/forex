package forex.broker

import forex.live.TestTrader
import forex.market.AccountOrderService
import forex.market.MarketTime
import forex.trader.TraderService
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.Month

import static forex.market.Instrument.EURUSD

class OrderServiceImplSpec extends Specification {

    def 'should use the specified units from the request'() {
        def id = '1'
        def traderService = Mock(TraderService)
        traderService.accountAndTrades(id, _) >> new AccountAndTrades(new AccountSummary(new Account.Builder(id)
                .withBalanceDollars(50)
                .withLastTransactionID('someId')
                .withProfitLoss(13L)
                .build(), [], Orders.empty()), [])
        def context = Mock(Context)
        context.getPricing(_) >> new PricingGetResponse([new Price(EURUSD, 10010L, 10020L)])

        def clock = Mock(MarketTime)
        def trader = new TestTrader(id, context, traderService, clock)

        OrderService service = new OrderServiceImpl(Mock(AccountOrderService))

        when: 'a trader opens a position with a specific number of units'
        service.openPosition(trader, new OpenPositionRequest(EURUSD, 3, null, null, null), Mock(Quote))

        then: 'the position is opened with the requested number of units'
        1 * context.createOrder({
            it.order.units == 3
        }) >> new OrderCreateResponse(EURUSD, new MarketOrderTransaction('6367', id,
                LocalDateTime.of(2016, Month.JUNE, 22, 13, 41, 29, 264030555), EURUSD, 3), null, null, null)
    }

}
