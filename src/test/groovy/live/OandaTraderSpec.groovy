package live

import broker.Account
import broker.AccountGetResponse
import broker.AccountID
import broker.Context
import broker.RequestException
import broker.TradeListResponse
import broker.TransactionID
import market.MarketTime
import spock.lang.Specification

import static trader.TradingStrategies.OPEN_RANDOM_POSITION

class OandaTraderSpec extends Specification {

    def 'should handle a request exception on refresh safely'() {
        String accountId = '12345'
        def accountID = new AccountID(accountId)

        Context ctx = Mock(Context)
        ctx.listTrade(_) >> new TradeListResponse([], null)

        MarketTime clock = Mock(MarketTime)
        Account retrievedLater = null

        when: 'the account is unavailable for refresh during construction'
        OandaTrader trader = new OandaTrader(accountId, ctx, OPEN_RANDOM_POSITION, clock)

        and: 'retrieving account happens again later'
        retrievedLater = trader?.account?.get()

        then: 'the trader instance was still constructed'
        trader

        and: 'it was able to be retrieved later'
        retrievedLater

        and: 'the first refresh failed'
        1 * ctx.getAccount(accountID) >> { throw new RequestException('Something bad happened') }

        and: 'the second succeeded'
        1 * ctx.getAccount(accountID) >> new AccountGetResponse(new Account(accountID, new TransactionID('someId'), [], 0L))
    }
}
