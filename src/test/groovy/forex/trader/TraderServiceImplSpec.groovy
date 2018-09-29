package forex.trader

import forex.broker.Account
import forex.broker.AccountChanges
import forex.broker.AccountChangesRequest
import forex.broker.AccountChangesResponse
import forex.broker.AccountChangesState
import forex.broker.Context
import forex.broker.RequestException
import forex.market.AccountRepository
import forex.market.TradeRepository
import spock.lang.Specification
import spock.lang.Unroll

class TraderServiceImplSpec extends Specification {

    static accountID = '1'

    @Unroll
    def 'should merge any existing changes while processing updates: #description'() {

        def currentAccount = new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID('3')
                .withProfitLoss(1L)
                .build()

        def context = Mock(Context)

        def accountRepository = Mock(AccountRepository)
        accountRepository.findById(accountID) >> Optional.of(currentAccount)
        accountRepository.getOne(accountID) >> currentAccount

        def tradeRepository = Mock(TradeRepository)
        tradeRepository.findByAccountIdAndCloseTimeIsNull(accountID) >> []
        tradeRepository.findByAccountIdAndCloseTimeIsNotNullOrderByCloseTimeDesc(accountID, _) >> []

        TraderServiceImpl traderService = new TraderServiceImpl(context, accountRepository, tradeRepository)

        when: 'the updates are processed'
        traderService.accountAndTrades(accountID, 10)

        then: 'the context is checked for changes'
        1 * context.accountChanges(new AccountChangesRequest(accountID,
                currentAccount.getLastTransactionID())) >> changes

        then: 'any changes were merged'
        (expected ? 1 : 0) * accountRepository.save(_ as Account)

        where:
        description                      | changes                                                                                               | expected
        'no changes'                     | new AccountChangesResponse('3', new AccountChanges([], []), new AccountChangesState(5000000, 0L, [])) | false
        'updated NAV and unrealized P&L' | new AccountChangesResponse('3', new AccountChanges([], []), new AccountChangesState(5000167, 0L, [])) | true
    }

    def 'should ignore expects exceptions occurring during account refresh'() {

        def currentAccount = new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID('3')
                .withProfitLoss(1L)
                .build()

        def context = Mock(Context)

        def accountRepository = Mock(AccountRepository)
        accountRepository.findById(accountID) >> Optional.of(currentAccount)
        accountRepository.getOne(accountID) >> currentAccount

        def tradeRepository = Mock(TradeRepository)
        tradeRepository.findByAccountIdAndCloseTimeIsNull(accountID) >> []
        tradeRepository.findByAccountIdAndCloseTimeIsNotNullOrderByCloseTimeDesc(accountID, _) >> []

        TraderServiceImpl traderService = new TraderServiceImpl(context, accountRepository, tradeRepository)

        when: 'the updates are processed'
        def accountAndTrades = traderService.accountAndTrades(accountID, 10)

        then: 'an exception is thrown checking for changes'
        1 * context.accountChanges(new AccountChangesRequest(accountID,
                currentAccount.getLastTransactionID())) >> {
            throw new RequestException('The transaction ID range specified is invalid')
        }

        and: 'the existing account was returned'
        accountAndTrades.account.account == currentAccount
    }

    def 'should not ignore unexpected exceptions occurring during account refresh'() {

        def currentAccount = new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID('3')
                .withProfitLoss(1L)
                .build()

        def context = Mock(Context)

        def accountRepository = Mock(AccountRepository)
        accountRepository.findById(accountID) >> Optional.of(currentAccount)
        accountRepository.getOne(accountID) >> currentAccount

        def tradeRepository = Mock(TradeRepository)
        tradeRepository.findByAccountIdAndCloseTimeIsNull(accountID) >> []
        tradeRepository.findByAccountIdAndCloseTimeIsNotNullOrderByCloseTimeDesc(accountID, _) >> []

        TraderServiceImpl traderService = new TraderServiceImpl(context, accountRepository, tradeRepository)

        when: 'the updates are processed'
        def accountAndTrades = traderService.accountAndTrades(accountID, 10)

        then: 'an exception is thrown checking for changes'
        1 * context.accountChanges(new AccountChangesRequest(accountID,
                currentAccount.getLastTransactionID())) >> {
            throw new RequestException('Something unexpected happened')
        }

        and: 'the exception was thrown'
        thrown(RequestException)
    }
}
