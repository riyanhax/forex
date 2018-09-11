package broker

import spock.lang.Specification

import static java.time.LocalDateTime.now
import static market.Instrument.EURUSD

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

}
