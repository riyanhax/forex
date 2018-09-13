package live

import broker.Account
import broker.AccountAndTrades
import broker.AccountChanges
import broker.AccountChangesRequest
import broker.AccountChangesResponse
import broker.AccountChangesState
import broker.AccountID
import broker.Context
import broker.ForexBroker
import broker.RequestException
import broker.TransactionID
import market.MarketTime
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

import static java.time.Month.SEPTEMBER
import static trader.TradingStrategies.OPEN_RANDOM_POSITION

class TraderSpec extends Specification {

    static final AccountID accountID = new AccountID('1234')

    def 'should handle a request exception on refresh safely'() {

        Context ctx = Mock(Context)

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
        1 * ctx.initializeAccount(accountID.id, _) >> { throw new RequestException('Something bad happened') }

        and: 'the second succeeded'
        1 * ctx.initializeAccount(accountID.id, _) >> new AccountAndTrades(new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID(new TransactionID('1234'))
                .build(), [])
    }

    @Unroll
    def 'should merge any existing changes while processing updates: #description'() {

        def currentAccount = new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID(new TransactionID('3'))
                .withTrades([])
                .withProfitLoss(1L)
                .build()

        def context = Mock(Context)
        context.initializeAccount(accountID.id, _) >> new AccountAndTrades(currentAccount, [])

        def clock = Mock(MarketTime)
        clock.now() >> LocalDateTime.of(2018, SEPTEMBER, 11, 12, 0)

        def trader = new Trader('1234', context, OPEN_RANDOM_POSITION, clock)

        when: 'the updates are processed'
        trader.processUpdates(Mock(ForexBroker))

        then: 'the context is checked for changes'
        1 * context.accountChanges(new AccountChangesRequest(currentAccount.getId(),
                currentAccount.getLastTransactionID())) >> changes

        then: 'any changes were merged'
        (trader.account.get() != currentAccount) == expected

        where:
        description                      | changes                                                                                                                  | expected
        'no changes'                     | new AccountChangesResponse(new TransactionID('3'), new AccountChanges([], []), new AccountChangesState(5000000, 0L, [])) | false
        'updated NAV and unrealized P&L' | new AccountChangesResponse(new TransactionID('3'), new AccountChanges([], []), new AccountChangesState(5000167, 0L, [])) | true
    }

}
