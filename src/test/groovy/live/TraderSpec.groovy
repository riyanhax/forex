package live

import broker.Account
import broker.AccountChanges
import broker.AccountChangesRequest
import broker.AccountChangesResponse
import broker.AccountGetResponse
import broker.AccountID
import broker.Context
import broker.ForexBroker
import broker.RequestException
import broker.TradeListRequest
import broker.TradeListResponse
import broker.TradeSummary
import broker.TransactionID
import market.MarketTime
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

import static java.time.Month.SEPTEMBER
import static market.Instrument.EURUSD
import static market.Instrument.USDEUR
import static trader.TradingStrategies.OPEN_RANDOM_POSITION

class TraderSpec extends Specification {

    static final AccountID accountID = new AccountID('1234')

    def 'should handle a request exception on refresh safely'() {

        Context ctx = Mock(Context)
        ctx.listTrade(_) >> new TradeListResponse([], null)

        MarketTime clock = Mock(MarketTime)
        clock.now() >> LocalDateTime.of(2018, SEPTEMBER, 11, 12, 0)

        when: 'the account is unavailable for data initialization during construction'
        Trader trader = new Trader(accountID.id, ctx, OPEN_RANDOM_POSITION, clock)
        assert !trader.account.isPresent()

        and: 'updates are processed later'
        trader?.processUpdates(Mock(ForexBroker))

        then: 'the trader instance was still constructed'
        trader

        and: 'account data was able to be retrieved later'
        trader.account.isPresent()

        and: 'the first initialize failed'
        1 * ctx.getAccount(accountID) >> { throw new RequestException('Something bad happened') }

        and: 'the second succeeded'
        1 * ctx.getAccount(accountID) >> new AccountGetResponse(new Account(accountID, new TransactionID('someId'), [], 0L))
    }

    @Unroll
    def 'should merge any existing changes prior to returning the account: #description'() {

        def openTrades = 'open trade' == description ? [] : [
                new TradeSummary(USDEUR, 1, 86233L, 0L, 55L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542), null, '309')
        ]
        def currentAccount = new Account(accountID, new TransactionID('3'), openTrades, 1L)

        def context = Mock(Context)
        context.getAccount(accountID) >> new AccountGetResponse(currentAccount)
        context.listTrade(_ as TradeListRequest) >> new TradeListResponse([], new TransactionID('3'))

        def clock = Mock(MarketTime)
        clock.now() >> LocalDateTime.of(2018, SEPTEMBER, 11, 12, 0)

        def trader = new Trader('1234', context, OPEN_RANDOM_POSITION, clock)

        when: 'the updates are processed'
        trader.processUpdates(Mock(ForexBroker))

        then: 'the context is checked for changes'
        1 * context.accountChanges(new AccountChangesRequest(currentAccount.getId(),
                currentAccount.getLastTransactionID())) >> changes

        then: 'any changes were merged'
        trader.account.get() == expected

        where:
        description    | changes                                                                        | expected
        'no changes'   | new AccountChangesResponse(new TransactionID('3'), new AccountChanges([], [])) | new Account(accountID, new TransactionID('3'), [
                new TradeSummary(USDEUR, 1, 86233L, 0L, 55L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542), null, '309')
        ], 1L)

        'closed trade' | new AccountChangesResponse(new TransactionID('4'), new AccountChanges([
                new TradeSummary(USDEUR, 1, 86233L, 6L, 0L,
                        LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542),
                        LocalDateTime.of(2018, SEPTEMBER, 7, 07, 45, 11, 338759441), '309')
        ], []))                                                                                         | new Account(accountID, new TransactionID('4'), [], 7L)

        'open trade'   | new AccountChangesResponse(new TransactionID('4'), new AccountChanges([], [
                new TradeSummary(EURUSD, 2, 115879L, 150L, 0L,
                        LocalDateTime.of(2018, SEPTEMBER, 7, 7, 31, 9, 524922739),
                        LocalDateTime.of(2018, SEPTEMBER, 7, 07, 42, 34, 554252280), '303')

        ]))                                                                                             | new Account(accountID, new TransactionID('4'), [
                new TradeSummary(EURUSD, 2, 115879L, 150L, 0L,
                        LocalDateTime.of(2018, SEPTEMBER, 7, 7, 31, 9, 524922739),
                        LocalDateTime.of(2018, SEPTEMBER, 7, 07, 42, 34, 554252280), '303')
        ], 1L)
    }

}
