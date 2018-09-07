package live.oanda

import broker.Account
import broker.AccountGetResponse
import broker.AccountID
import broker.TradeSummary
import broker.TransactionID
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oanda.v20.order.OrderAdapter
import com.oanda.v20.transaction.TransactionAdapter
import live.oanda.AccountConverter
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.Month

import static market.Instrument.EURUSD

class AccountConverterSpec extends Specification {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(com.oanda.v20.order.Order.class, new OrderAdapter())
            .registerTypeAdapter(com.oanda.v20.transaction.Transaction.class, new TransactionAdapter())
            .create();

    def 'should convert account get response correctly'() {

        def expected = new AccountGetResponse(new Account(new AccountID("101-001-1775714-008"), new TransactionID("293"), [
                new TradeSummary(EURUSD, 2, 116085L, 0L, 110L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 6, 27, 48, 889977095), null, '291')
        ], 1890L))

        def json = getClass().getResourceAsStream('AccountGetResponse.json').text
        def response = gson.fromJson(json, com.oanda.v20.account.AccountGetResponse.class)

        AccountGetResponse actual = AccountConverter.convert(response)

        expect:
        actual == expected
    }
}
