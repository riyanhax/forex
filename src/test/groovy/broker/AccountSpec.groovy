package broker


import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

import static java.time.LocalDateTime.now
import static java.time.Month.SEPTEMBER
import static market.Instrument.EURUSD
import static market.Instrument.USDEUR

class AccountSpec extends Specification {

    static final accountID = new AccountID('1234')
    static final newTransactionID = new TransactionID('2')

    def 'should modify correctly for opened positions'() {

        def position = new TradeSummary(EURUSD, 2, 116043L, 0L, -20L, now(), null, '100')

        def current = new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID(new TransactionID('1'))
                .build()
        def actual = current.positionOpened(position, newTransactionID)

        expect:
        actual == new Account(accountID, 4767914L, 4999980L, new TransactionID('2'), [
                position
        ], 0L)
    }

    def 'should modify correctly for closed positions'() {

        // Closed total of 7 pips from purchase (9 total pips including spread)
        def position = new TradeSummary(EURUSD, 2, 116043L, -180L, 0L, now().minusMinutes(33), now(), '100')

        def current = new Account.Builder(accountID)
                .withBalance(4767954L)
                .withLastTransactionID(new TransactionID('1'))
                .withTrades([new TradeSummary(EURUSD, 2, 116043L, 0L, -20L, now(), null, '100')])
                .build()

        def actual = current.positionClosed(position, newTransactionID)

        expect:
        actual == new Account(accountID, 4999860, 4999860, new TransactionID('2'), [], -180L)
    }

    @Unroll
    def 'should calculate net asset value based on account balance and open trade value: #expected'() {

        def account = new Account.Builder(accountID)
                .withBalance(4767954L)
                .withTrades(trades)
                .build()

        def actual = Account.calculateNav(account.getBalance(), account.getTrades())

        expect:
        actual == expected

        where:
        expected | trades
        5232116L | [
                new TradeSummary(EURUSD, 2, 116043L, 0L, -10L, now(), null, '100'),
                new TradeSummary(EURUSD, 2, 116053L, 0L, -20L, now(), null, '101')
        ]
        5232136L | [
                new TradeSummary(EURUSD, 2, 116058, 0L, -15L, now(), null, '100'),
                new TradeSummary(EURUSD, 2, 116028, 0L, 25L, now(), null, '101')
        ]
    }

    @Unroll
    def 'should calculate unrealized P&L based on open trade unrealized P&L: #expected'() {

        def account = new Account.Builder(accountID)
                .withBalance(4767954L)
                .withTrades(trades)
                .build()

        def actual = account.getUnrealizedProfitLoss();

        expect:
        actual == expected

        where:
        expected | trades
        -30      | [
                new TradeSummary(EURUSD, 2, 116043L, 0L, -10L, now(), null, '100'),
                new TradeSummary(EURUSD, 2, 116053L, 0L, -20L, now(), null, '101')
        ]
        10       | [
                new TradeSummary(EURUSD, 2, 116058, 0L, -15L, now(), null, '100'),
                new TradeSummary(EURUSD, 2, 116028, 0L, 25L, now(), null, '101')
        ]
    }

    @Unroll
    def 'should merge account changes correctly: #description'() {

        def testOpeningTrade = 'open trade' == description

        def balance = testOpeningTrade ? 5000000L : 4913822L
        def openTrades = testOpeningTrade ? [] : [
                new TradeSummary(USDEUR, 1, 86288L, 0L, 55L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542), null, '309')
        ]
        def nav = balance
        nav += (openTrades.isEmpty() ? 0L : openTrades[0].unrealizedProfitLoss + openTrades[0].price)

        def account = new Account.Builder(accountID)
                .withBalance(balance)
                .withNetAssetValue(nav)
                .withLastTransactionID(new TransactionID('3'))
                .withTrades(openTrades)
                .withProfitLoss(1L)
                .build()

        when: 'account changes are processed'
        def actual = account.processChanges(changes)

        then: 'changes were merged correctly'
        actual == expected

        where:
        description                      | changes                                                                                                                                                       | expected
        'no changes'                     | new AccountChangesResponse(new TransactionID('3'), new AccountChanges([], []), new AccountChangesState(5000165, 55L, []))                                     | new Account(accountID, 4913822L, 5000165L, new TransactionID('3'), [
                new TradeSummary(USDEUR, 1, 86288L, 0L, 55L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542), null, '309')
        ], 1L)

        'updated NAV and unrealized P&L' | new AccountChangesResponse(new TransactionID('3'), new AccountChanges([], []), new AccountChangesState(5000167, 57L, [new CalculatedTradeState('309', 57L)])) | new Account(accountID, 4913822L, 5000167L, new TransactionID('3'), [
                new TradeSummary(USDEUR, 1, 86288L, 0L, 57L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542), null, '309')
        ], 1L)

        'closed trade'                   | new AccountChangesResponse(new TransactionID('4'), new AccountChanges([
                new TradeSummary(USDEUR, 1, 86288L, 63L, 0L,
                        LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542),
                        LocalDateTime.of(2018, SEPTEMBER, 7, 7, 45, 11, 338759441), '309')
        ], []), new AccountChangesState(5000173, 0L, []))                                                                                                                                                | new Account(accountID, 5000173L, 5000173L, new TransactionID('4'), [], 64L)

        'open trade'                     | new AccountChangesResponse(new TransactionID('4'), new AccountChanges([], [
                new TradeSummary(EURUSD, 2, 116029L, 0L, 150L,
                        LocalDateTime.of(2018, SEPTEMBER, 7, 7, 31, 9, 524922739),
                        null, '303')

        ]), new AccountChangesState(5000150, 150L, [new CalculatedTradeState('303', 150L)]))                                                                                                             | new Account(accountID, 4767942, 5000150, new TransactionID('4'), [
                new TradeSummary(EURUSD, 2, 116029L, 0L, 150L,
                        LocalDateTime.of(2018, SEPTEMBER, 7, 7, 31, 9, 524922739),
                        null, '303')
        ], 1L)
    }

    @Unroll
    def 'should update account balance when a broker NAV discrepancy exists'() {

        def actual = new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withNetAssetValueDollars(50)
                .withTrades([])
                .build()
                .processStateChanges(stateChanges)

        expect:
        actual == expected

        where:
        stateChanges                              | expected
        new AccountChangesState(5000000L, 0L, []) | new Account.Builder(accountID).withBalanceDollars(50).withNetAssetValueDollars(50).build()
        new AccountChangesState(5001000L, 0L, []) | new Account.Builder(accountID).withBalance(5001000L).withNetAssetValue(5001000L).build()
        new AccountChangesState(4991000L, 0L, []) | new Account.Builder(accountID).withBalance(4991000L).withNetAssetValue(4991000L).build()
    }
}
