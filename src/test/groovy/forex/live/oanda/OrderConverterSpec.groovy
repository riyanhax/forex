package forex.live.oanda

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oanda.v20.order.OrderAdapter
import com.oanda.v20.transaction.TransactionAdapter
import forex.broker.MarketOrderTransaction
import forex.broker.OrderCancelTransaction
import forex.broker.OrderCreateResponse
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

import static forex.broker.OrderCancelReason.MARKET_HALTED
import static forex.market.Instrument.EURUSD
import static forex.market.Instrument.USDEUR
import static java.time.Month.JUNE
import static java.time.Month.SEPTEMBER

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
        instrument | responseFile                        | expected
        EURUSD     | 'OrderCreateResponse-Long.json'     | new OrderCreateResponse(EURUSD, new MarketOrderTransaction('6367',
                LocalDateTime.of(2016, JUNE, 22, 13, 41, 29, 264030555), EURUSD, 100), null)

        USDEUR     | 'OrderCreateResponse-Short.json'    | new OrderCreateResponse(USDEUR, new MarketOrderTransaction('6367',
                LocalDateTime.of(2016, JUNE, 22, 13, 41, 29, 264030555), USDEUR, 100), null)

        USDEUR     | 'OrderCreateResponse-Canceled.json' | new OrderCreateResponse(USDEUR, new MarketOrderTransaction('1075',
                LocalDateTime.of(2018, SEPTEMBER, 29, 14, 56, 3, 566789846), USDEUR, 1),
                new OrderCancelTransaction('1075', MARKET_HALTED, '1076', '60495087698774865',
                        LocalDateTime.of(2018, SEPTEMBER, 29, 14, 56, 3, 566789846))
        )
    }
}
