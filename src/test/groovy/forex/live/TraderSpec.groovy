package forex.live

import forex.broker.Account
import forex.broker.AccountAndTrades
import forex.broker.AccountChanges
import forex.broker.AccountChangesRequest
import forex.broker.AccountChangesResponse
import forex.broker.AccountChangesState
import forex.broker.Context
import forex.broker.ForexBroker
import forex.broker.RequestException
import forex.market.MarketTime
import forex.trader.Trader
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

import static forex.trader.TradingStrategies.OPEN_RANDOM_POSITION
import static java.time.Month.SEPTEMBER

class TraderSpec extends Specification {

    static final String accountID = '1234'

    def 'should handle a request exception on refresh safely'() {

        Context ctx = Mock(Context)

        MarketTime clock = Mock(MarketTime)
        clock.now() >> LocalDateTime.of(2018, SEPTEMBER, 11, 12, 0)

        when: 'the account is unavailable for data initialization during construction'
        Trader trader = new Trader(accountID, ctx, OPEN_RANDOM_POSITION, clock)
        assert !trader.account.isPresent()

        and: 'updates are processed later'
        trader?.processUpdates(Mock(ForexBroker))

        then: 'the trader instance was still constructed'
        trader

        and: 'account data was able to be retrieved later'
        trader.account.isPresent()

        and: 'the first initialize failed'
        1 * ctx.initializeAccount(accountID, _) >> { throw new RequestException('Something bad happened') }

        and: 'the second succeeded'
        1 * ctx.initializeAccount(accountID, _) >> new AccountAndTrades(new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID('1234')
                .build(), [])
    }

    @Unroll
    def 'should merge any existing changes while processing updates: #description'() {

        def currentAccount = new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID('3')
                .withTrades([])
                .withProfitLoss(1L)
                .build()

        def context = Mock(Context)
        context.initializeAccount(accountID, _) >> new AccountAndTrades(currentAccount, [])

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
        'no changes'                     | new AccountChangesResponse('3', new AccountChanges([], []), new AccountChangesState(5000000, 0L, [])) | false
        'updated NAV and unrealized P&L' | new AccountChangesResponse('3', new AccountChanges([], []), new AccountChangesState(5000167, 0L, [])) | true
    }

}
