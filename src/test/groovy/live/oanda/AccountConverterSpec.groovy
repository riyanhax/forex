package live.oanda

import broker.Account
import broker.AccountChanges
import broker.AccountChangesResponse
import broker.AccountChangesState
import broker.AccountGetResponse
import broker.AccountID
import broker.CalculatedTradeState
import broker.TradeSummary
import broker.TransactionID
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oanda.v20.order.OrderAdapter
import com.oanda.v20.transaction.TransactionAdapter
import spock.lang.Specification

import java.time.LocalDateTime

import static java.time.Month.SEPTEMBER
import static market.Instrument.EURUSD
import static market.Instrument.USDEUR

class AccountConverterSpec extends Specification {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(com.oanda.v20.order.Order.class, new OrderAdapter())
            .registerTypeAdapter(com.oanda.v20.transaction.Transaction.class, new TransactionAdapter())
            .create();

    def 'should convert account get response correctly'() {

        def expected = new AccountGetResponse(new Account.Builder(new AccountID("101-001-1775714-008"))
                .withBalance(5001930L)
                .withLastTransactionID(new TransactionID('293'))
                .withTrades([new TradeSummary('291', EURUSD, 116085L, LocalDateTime.of(2018, SEPTEMBER, 7, 6, 27, 48, 889977095), 2, 2, 0L, 110L, null)])
                .withProfitLoss(1890L)
                .build())

        def json = getClass().getResourceAsStream('AccountGetResponse.json').text
        def response = gson.fromJson(json, com.oanda.v20.account.AccountGetResponse.class)

        AccountGetResponse actual = AccountConverter.convert(response)

        expect:
        actual == expected
    }

    def 'should convert account changes response correctly'() {

        def expectedTrades = [
                new CalculatedTradeState("997", -10L)
        ]

        def expected = new AccountChangesResponse(new TransactionID("999"), new AccountChanges([
                new TradeSummary('993', USDEUR, 86402L, LocalDateTime.of(2018, SEPTEMBER, 7, 10, 49, 6,
                        159247625), 1, 0, -20L, 0L, LocalDateTime.of(2018, SEPTEMBER, 7, 10, 50, 30, 782081491))
        ], [
                new TradeSummary('997', USDEUR, 86395L, LocalDateTime.of(2018, SEPTEMBER, 7, 10, 50, 43,
                        289257), 1, 1, 0L, 0L, null)
        ]), new AccountChangesState(50216480L, -10L, expectedTrades))

        def json = getClass().getResourceAsStream('AccountChangesResponse.json').text
        def response = gson.fromJson(json, com.oanda.v20.account.AccountChangesResponse.class)

        AccountChangesResponse actual = AccountConverter.convert(response)

        expect:
        actual == expected
    }
}
