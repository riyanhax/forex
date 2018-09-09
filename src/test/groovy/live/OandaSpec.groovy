package live

import broker.Account
import broker.AccountGetResponse
import broker.AccountID
import broker.Context
import broker.MarketOrderRequest
import broker.MarketOrderTransaction
import broker.OpenPositionRequest
import broker.OrderCreateResponse
import broker.Price
import broker.PricingGetResponse
import broker.Quote
import broker.StopLossDetails
import broker.TakeProfitDetails
import broker.TradeListResponse
import broker.TransactionID
import market.AccountSnapshot
import market.MarketTime
import simulator.TestClock
import spock.lang.Specification
import spock.lang.Unroll
import trader.ForexTrader

import java.time.LocalDateTime
import java.time.Month

import static java.time.Month.APRIL
import static java.time.Month.NOVEMBER
import static market.Instrument.EURUSD
import static market.Instrument.USDEUR

class OandaSpec extends Specification {

    @Unroll
    def 'should evaluate isClosed correctly: #description'() {

        def clock = Mock(MarketTime)
        clock.now() >> now

        def broker = new Oanda(clock, new LiveTraders([]))
        def actual = broker.isClosed()

        expect:
        actual == expected

        where:
        description                             | now                                              | expected
        'spring friday one second before close' | LocalDateTime.of(2018, APRIL, 6, 15, 59, 59)     | false
        'spring friday at close'                | LocalDateTime.of(2018, APRIL, 6, 16, 0, 0)       | true
        'spring saturday'                       | LocalDateTime.of(2018, APRIL, 7, 12, 0, 0)       | true
        'spring sunday 1 hour before open'      | LocalDateTime.of(2018, APRIL, 8, 15, 0, 0)       | true
        'spring sunday 1 second before open'    | LocalDateTime.of(2018, APRIL, 8, 15, 59, 59)     | true
        'spring sunday at open'                 | LocalDateTime.of(2018, APRIL, 8, 16, 0, 0)       | false
        'spring sunday 1 hour after open'       | LocalDateTime.of(2018, APRIL, 8, 17, 0, 0)       | false

        'fall friday one second before close'   | LocalDateTime.of(2018, NOVEMBER, 16, 15, 59, 59) | false
        'fall friday at close'                  | LocalDateTime.of(2018, NOVEMBER, 16, 16, 0, 0)   | true
        'fall saturday'                         | LocalDateTime.of(2018, NOVEMBER, 17, 12, 0, 0)   | true
        'fall sunday 1 hour before open'        | LocalDateTime.of(2018, NOVEMBER, 18, 15, 0, 0)   | true
        'fall sunday 1 second before open'      | LocalDateTime.of(2018, NOVEMBER, 18, 15, 59, 59) | true
        'fall sunday at open'                   | LocalDateTime.of(2018, NOVEMBER, 18, 16, 0, 0)   | false
        'fall sunday 1 hour after open'         | LocalDateTime.of(2018, NOVEMBER, 18, 17, 0, 0)   | false
    }

    @Unroll
    def 'should not process traders when the broker is closed, open: #open'() {

        ForexTrader trader = Mock(ForexTrader)
        trader.accountNumber >> '1234'

        boolean closed = !open
        def broker = new Oanda(Mock(MarketTime), new LiveTraders([trader])) {
            @Override
            boolean isClosed() {
                return closed
            }
        }

        when: 'if the broker is closed'
        broker.processUpdates()

        then: 'traders are not processed'
        (open ? 1 : 0) * trader.processUpdates(broker)

        where:
        open << [true, false]
    }

    def 'should return account snapshot data associated to the current time'() {
        def accountID = new AccountID('accountId')
        def expectedAccountData = new Account(accountID, new TransactionID('1234'), [], 13L)

        def trader = Mock(ForexTrader)
        trader.accountNumber >> expectedAccountData.id.id
        trader.account >> Optional.of(expectedAccountData)

        def clock = new TestClock(LocalDateTime.now())

        def traders = new LiveTraders([trader])
        def broker = new Oanda(clock, traders)

        when: 'an account snapshot is requested for a trader'
        def actual = broker.getAccountSnapshot(traders.traders[0])

        then: 'account data is returned with the current timestamp'
        actual == new AccountSnapshot(expectedAccountData, clock.now())
    }

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
        context.listTrade(_) >> new TradeListResponse([], null)
        context.getAccount(_) >> new AccountGetResponse(new Account(new AccountID('someAccountId'), new TransactionID('someId'), [], 0L))
        context.getPricing(_) >> new PricingGetResponse([new Price(EURUSD, 10010L, 10020L)])

        def clock = Mock(MarketTime)
        def trader = new TestTrader(context, clock)

        Oanda oanda = new Oanda(clock, new LiveTraders([trader]))

        when: 'a trader opens a position with a specific number of units'
        oanda.openPosition(trader, new OpenPositionRequest(EURUSD, 3, null, null, null))

        then: 'the position is opened with the requested number of units'
        1 * context.createOrder({
            it.order.units == 3
        }) >> new OrderCreateResponse(EURUSD, new MarketOrderTransaction('6367',
                LocalDateTime.of(2016, Month.JUNE, 22, 13, 41, 29, 264030555), EURUSD, 3))
    }
}
