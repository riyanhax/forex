package live

import broker.Account
import broker.AccountChanges
import broker.AccountChangesRequest
import broker.AccountChangesResponse
import broker.AccountChangesState
import broker.AccountGetResponse
import broker.AccountID
import broker.CalculatedTradeState
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
        1 * ctx.getAccount(accountID) >> new AccountGetResponse(new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID(new TransactionID('1234'))
                .build())
    }

    @Unroll
    def 'should merge any existing changes prior to returning the account: #description'() {

        def testOpeningTrade = 'open trade' == description

        def balance = testOpeningTrade ? 5000000L : 4913822L
        def openTrades = testOpeningTrade ? [] : [
                new TradeSummary(USDEUR, 1, 86288L, 0L, 55L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542), null, '309')
        ]
        def nav = balance
        nav += (openTrades.isEmpty() ? 0L : openTrades[0].unrealizedProfitLoss + openTrades[0].price)

        def currentAccount = new Account.Builder(accountID)
                .withBalance(balance)
                .withNetAssetValue(nav)
                .withLastTransactionID(new TransactionID('3'))
                .withTrades(openTrades)
                .withProfitLoss(1L)
                .build()

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
        description                      | changes                                                                                                                                                        | expected
        'no changes'                     | new AccountChangesResponse(new TransactionID('3'), new AccountChanges([], []), new AccountChangesState(5000165, 55L, []))                                      | new Account(accountID, 4913822L, 5000165L, new TransactionID('3'), [
                new TradeSummary(USDEUR, 1, 86288L, 0L, 55L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542), null, '309')
        ], 1L)

        // TODO DPJ: Update account nav and unrealized P&L
        'updated NAV and unrealized P&L' | new AccountChangesResponse(new TransactionID('3'), new AccountChanges([], []), new AccountChangesState(5000167, 57L, [new CalculatedTradeState('309', 57L)])) | new Account(accountID, 4913822L, 5000167L, new TransactionID('3'), [
                new TradeSummary(USDEUR, 1, 86288L, 0L, 57L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542), null, '309')
        ], 1L)

        'closed trade'                   | new AccountChangesResponse(new TransactionID('4'), new AccountChanges([
                new TradeSummary(USDEUR, 1, 86288L, 63L, 0L,
                        LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542),
                        LocalDateTime.of(2018, SEPTEMBER, 7, 7, 45, 11, 338759441), '309')
        ], []), new AccountChangesState(5000173, 0L, []))                                                                                                                                                 | new Account(accountID, 5000173L, 5000173L, new TransactionID('4'), [], 64L)

        'open trade'                     | new AccountChangesResponse(new TransactionID('4'), new AccountChanges([], [
                new TradeSummary(EURUSD, 2, 116029L, 0L, 150L,
                        LocalDateTime.of(2018, SEPTEMBER, 7, 7, 31, 9, 524922739),
                        null, '303')

        ]), new AccountChangesState(5000150, 150L, [new CalculatedTradeState('303', 150L)]))                                                                                                              | new Account(accountID, 4767942, 5000150, new TransactionID('4'), [
                new TradeSummary(EURUSD, 2, 116029L, 0L, 150L,
                        LocalDateTime.of(2018, SEPTEMBER, 7, 7, 31, 9, 524922739),
                        null, '303')
        ], 1L)
    }

}
