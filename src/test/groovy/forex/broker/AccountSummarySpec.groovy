package forex.broker

import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

import static forex.market.Instrument.EURUSD
import static forex.market.Instrument.USDEUR
import static java.time.LocalDateTime.now
import static java.time.Month.SEPTEMBER

class AccountSummarySpec extends Specification {

    static final accountID = '1234'
    static final newTransactionID = '2'

    def 'should modify correctly for opened positions'() {

        def position = new TradeSummary('100', accountID, EURUSD, 116043L, now(), 2, 2, 0L, -20L, null)

        def account = new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID('1')
                .build()

        def current = new AccountSummary(account, [])
        def actual = current.positionOpened(position, newTransactionID)

        expect:
        actual == new AccountSummary(new Account(accountID, 4767914L, '2', 0L), [position])
    }

    def 'should modify correctly for closed positions'() {

        // Closed total of 7 pips from purchase (9 total pips including spread)
        def position = new TradeSummary('100', accountID, EURUSD, 116043L, now().minusMinutes(33), 2, 0, -180L, 0L, now())

        def trades = [new TradeSummary('100', accountID, EURUSD, 116043L, now(), 2, 2, 0L, -20L, null)]
        def account = new Account.Builder(accountID)
                .withBalance(4767954L)
                .withLastTransactionID('1')
                .build()
        def current = new AccountSummary(account, trades)

        def actual = current.positionClosed(position, newTransactionID)

        expect:
        actual == new AccountSummary(new Account(accountID, 4999860, '2', -180L), [])
    }

    @Unroll
    def 'should calculate net asset value based on account balance and open trade value: #expected'() {

        def account = new Account.Builder(accountID)
                .withBalance(4767954L)
                .build()

        def summary = new AccountSummary(account, trades)
        def actual = summary.getNetAssetValue()

        expect:
        actual == expected

        where:
        expected | trades
        5232116L | [
                new TradeSummary('100', accountID, EURUSD, 116043L, now(), 2, 2, 0L, -10L, null),
                new TradeSummary('101', accountID, EURUSD, 116053L, now(), 2, 2, 0L, -20L, null)
        ]
        5232136L | [
                new TradeSummary('100', accountID, EURUSD, 116058, now(), 2, 2, 0L, -15L, null),
                new TradeSummary('101', accountID, EURUSD, 116028, now(), 2, 2, 0L, 25L, null)
        ]
    }

    @Unroll
    def 'should calculate unrealized P&L based on open trade unrealized P&L: #expected'() {

        def account = new Account.Builder(accountID)
                .withBalance(4767954L)
                .build()
        def summary = new AccountSummary(account, trades)

        def actual = summary.getUnrealizedProfitLoss();

        expect:
        actual == expected

        where:
        expected | trades
        -30      | [
                new TradeSummary('100', accountID, EURUSD, 116043L, now(), 2, 2, 0L, -10L, null),
                new TradeSummary('101', accountID, EURUSD, 116053L, now(), 2, 2, 0L, -20L, null)
        ]
        10       | [
                new TradeSummary('100', accountID, EURUSD, 116058, now(), 2, 2, 0L, -15L, null),
                new TradeSummary('101', accountID, EURUSD, 116028, now(), 2, 2, 0L, 25L, null)
        ]
    }

    @Unroll
    def 'should merge account changes correctly: #description'() {

        def testOpeningTrade = 'open trade' == description

        def balance = testOpeningTrade ? 5000000L : 4913822L
        def openTrades = testOpeningTrade ? [] : [
                new TradeSummary('309', accountID, USDEUR, 86288L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542), 1, 1, 0L, 55L, null)
        ]

        def account = new Account.Builder(accountID)
                .withBalance(balance)
                .withLastTransactionID('3')
                .withProfitLoss(1L)
                .build()
        def summary = new AccountSummary(account, openTrades)

        when: 'account changes are processed'
        def actual = summary.processChanges(changes)

        then: 'changes were merged correctly'
        actual == expected

        where:
        description                      | changes                                                                                                                                    | expected
        'no changes'                     | new AccountChangesResponse('3', new AccountChanges([], []), new AccountChangesState(5000165, 55L, []))                                     | new AccountSummary(new Account(accountID, 4913822L, '3', 1L), [
                new TradeSummary('309', accountID, USDEUR, 86288L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542), 1, 1, 0L, 55L, null)
        ])

        'updated NAV and unrealized P&L' | new AccountChangesResponse('3', new AccountChanges([], []), new AccountChangesState(5000167, 57L, [new CalculatedTradeState('309', 57L)])) | new AccountSummary(new Account(accountID, 4913822L, '3', 1L), [
                new TradeSummary('309', accountID, USDEUR, 86288L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542), 1, 1, 0L, 57L, null)
        ])

        'closed trade'                   | new AccountChangesResponse('4', new AccountChanges([
                new TradeSummary('309', accountID, USDEUR, 86288L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542), 1, 0, 63L, 0L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 45, 11, 338759441)
                )
        ], []), new AccountChangesState(5000173, 0L, []))                                                                                                                             | new AccountSummary(new Account(accountID, 5000173L, '4', 64L), [])

        'open trade'                     | new AccountChangesResponse('4', new AccountChanges([], [
                new TradeSummary('303', accountID, EURUSD, 116029L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 31, 9, 524922739), 2, 2, 0L, 150L, null)

        ]), new AccountChangesState(5000150, 150L, [new CalculatedTradeState('303', 150L)]))                                                                                          | new AccountSummary(new Account(accountID, 4767942, '4', 1L), [
                new TradeSummary('303', accountID, EURUSD, 116029L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 31, 9, 524922739), 2, 2, 0L, 150L, null)
        ])
    }

    @Unroll
    def 'should update account balance when a broker NAV discrepancy exists'() {

        def account = new Account.Builder(accountID)
                .withBalanceDollars(50)
                .build()

        def actual = new AccountSummary(account, []).processStateChanges(stateChanges)


        expect:
        actual == expected

        where:
        stateChanges                              | expected
        new AccountChangesState(5000000L, 0L, []) | new AccountSummary(new Account.Builder(accountID).withBalanceDollars(50).build(), [])
        new AccountChangesState(5001000L, 0L, []) | new AccountSummary(new Account.Builder(accountID).withBalance(5001000L).build(), [])
        new AccountChangesState(4991000L, 0L, []) | new AccountSummary(new Account.Builder(accountID).withBalance(4991000L).build(), [])
    }
}
