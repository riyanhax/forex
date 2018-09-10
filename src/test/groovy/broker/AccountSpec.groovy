package broker

import spock.lang.Specification

import static java.time.LocalDateTime.now
import static market.Instrument.EURUSD

class AccountSpec extends Specification {

    static final accountID = new AccountID('1234')
    static final newTransactionID = new TransactionID('2')

    def 'should modify correctly for opened positions'() {

        def position = new TradeSummary(EURUSD, 2, 116023L, 0L, -20L, now(), null, '100')

        def current = new Account(accountID, 5000000L, new TransactionID('1'), [], 0L)
        def actual = current.positionOpened(position, newTransactionID)

        expect:
        actual == new Account(accountID, 4767954L, new TransactionID('2'), [
                position
        ], 0L)
    }

    def 'should modify correctly for closed positions'() {

        // Closed total of 7 pips from purchase (9 total pips including spread)
        def position = new TradeSummary(EURUSD, 2, 115933L, -180L, 0L, now().minusMinutes(33), now(), '100')

        def current = new Account(accountID, 4767954L, new TransactionID('1'), [
                new TradeSummary(EURUSD, 2, 116023L, 0L, -20L, now(), null, '100')
        ], 0L)

        def actual = current.positionClosed(position, newTransactionID)

        expect:
        actual == new Account(accountID, 4999820L, new TransactionID('2'), [], -180L)
    }

}
