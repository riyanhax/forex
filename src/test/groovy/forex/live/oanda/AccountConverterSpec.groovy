package forex.live.oanda

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oanda.v20.order.OrderAdapter
import com.oanda.v20.transaction.TransactionAdapter
import forex.broker.Account
import forex.broker.AccountChanges
import forex.broker.AccountChangesResponse
import forex.broker.AccountChangesState
import forex.broker.AccountGetResponse
import forex.broker.AccountSummary
import forex.broker.CalculatedTradeState
import forex.broker.TradeSummary
import spock.lang.Specification

import java.time.LocalDateTime

import static forex.market.Instrument.EURUSD
import static forex.market.Instrument.USDEUR
import static java.time.Month.SEPTEMBER

class AccountConverterSpec extends Specification {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(com.oanda.v20.order.Order.class, new OrderAdapter())
            .registerTypeAdapter(com.oanda.v20.transaction.Transaction.class, new TransactionAdapter())
            .create();

    def 'should convert account get response correctly'() {

        def accountId = '101-001-1775714-008'
        def expected = new AccountGetResponse(new AccountSummary(new Account.Builder(accountId)
                .withBalance(5001930L)
                .withLastTransactionID('293')
                .withProfitLoss(1890L)
                .build(), [new TradeSummary('291', accountId, EURUSD, 116085L, LocalDateTime.of(2018, SEPTEMBER, 7, 6, 27, 48, 889977095), 2, 2, 0L, 110L, null)]))

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

        def accountID = '1'
        def expected = new AccountChangesResponse("999", new AccountChanges([
                new TradeSummary('993', accountID, USDEUR, 86402L, LocalDateTime.of(2018, SEPTEMBER, 7, 10, 49, 6,
                        159247625), 1, 0, -20L, 0L, LocalDateTime.of(2018, SEPTEMBER, 7, 10, 50, 30, 782081491))
        ], [
                new TradeSummary('997', accountID, USDEUR, 86395L, LocalDateTime.of(2018, SEPTEMBER, 7, 10, 50, 43,
                        289257), 1, 1, 0L, 0L, null)
        ]), new AccountChangesState(50216480L, -10L, expectedTrades))

        def json = getClass().getResourceAsStream('AccountChangesResponse.json').text
        com.oanda.v20.account.AccountChangesResponse response = gson.fromJson(json, com.oanda.v20.account.AccountChangesResponse.class)

        AccountChangesResponse actual = AccountConverter.convert(response, accountID)

        expect:
        actual == expected
    }
}
