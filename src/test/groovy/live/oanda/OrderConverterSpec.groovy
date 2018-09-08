package live.oanda

import broker.MarketOrderTransaction
import broker.OrderCreateResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oanda.v20.order.OrderAdapter
import com.oanda.v20.transaction.TransactionAdapter
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.Month

import static market.Instrument.EURUSD
import static market.Instrument.USDEUR

class OrderConverterSpec extends Specification {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(com.oanda.v20.order.Order.class, new OrderAdapter())
            .registerTypeAdapter(com.oanda.v20.transaction.Transaction.class, new TransactionAdapter())
            .create();

    @Unroll
    def 'should convert order create response correctly: #instrument'() {

        def json = getClass().getResourceAsStream(responseFile).text
        def response = gson.fromJson(json, com.oanda.v20.order.OrderCreateResponse.class)

        OrderCreateResponse actual = OrderConverter.convert(instrument, response)

        expect:
        actual == expected

        where:
        instrument | responseFile                     | expected
        EURUSD     | 'OrderCreateResponse-Long.json'  | new OrderCreateResponse(EURUSD, new MarketOrderTransaction('6367',
                LocalDateTime.of(2016, Month.JUNE, 22, 13, 41, 29, 264030555), EURUSD, 100))

        USDEUR     | 'OrderCreateResponse-Short.json' | new OrderCreateResponse(USDEUR, new MarketOrderTransaction('6367',
                LocalDateTime.of(2016, Month.JUNE, 22, 13, 41, 29, 264030555), USDEUR, 100))
    }
}
