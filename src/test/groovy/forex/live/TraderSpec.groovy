package forex.live

import forex.broker.Account
import forex.broker.AccountAndTrades
import forex.broker.AccountSummary
import forex.broker.Context
import forex.broker.ForexBroker
import forex.broker.Orders
import forex.broker.RequestException
import forex.market.MarketTime
import forex.trader.Trader
import forex.trader.TraderService
import spock.lang.Specification

import java.time.LocalDateTime

import static forex.trader.TradingStrategies.OPEN_RANDOM_POSITION
import static java.time.Month.SEPTEMBER

class TraderSpec extends Specification {

    static final String accountID = '1234'

    def 'should handle a request exception on refresh safely'() {

        def broker = Mock(ForexBroker)
        Context ctx = Mock(Context)
        TraderService traderService = Mock(TraderService)

        MarketTime clock = Mock(MarketTime)
        clock.now() >> LocalDateTime.of(2018, SEPTEMBER, 11, 12, 0)

        Trader trader = new Trader(accountID, ctx, traderService, OPEN_RANDOM_POSITION, clock)

        when: 'the account is unavailable'
        trader.processUpdates(broker)
        assert !trader.account.isPresent()

        and: 'updates are processed later'
        trader.processUpdates(broker)

        then: 'account data was able to be retrieved later'
        trader.account.isPresent()

        and: 'the first initialize failed'
        1 * traderService.accountAndTrades(accountID, _) >> { throw new RequestException('Something bad happened') }

        and: 'the second succeeded'
        1 * traderService.accountAndTrades(accountID, _) >> new AccountAndTrades(new AccountSummary(new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID('1234')
                .build(), [], Orders.empty()), [])
    }

}
