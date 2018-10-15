package forex.live.oanda

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oanda.v20.order.OrderAdapter
import com.oanda.v20.transaction.TransactionAdapter
import forex.broker.LimitOrderTransaction
import forex.broker.MarketOrderTransaction
import forex.broker.OrderCancelTransaction
import forex.broker.OrderCreateResponse
import forex.broker.OrderFillTransaction
import spock.lang.Specification
import spock.lang.Unroll

import static forex.broker.OrderCancelReason.MARKET_HALTED
import static forex.market.Instrument.EURUSD
import static forex.market.Instrument.USDEUR
import static java.time.LocalDateTime.of as ldt
import static java.time.Month.JUNE
import static java.time.Month.OCTOBER
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
        instrument | responseFile                           | expected
        EURUSD     | 'OrderCreateResponse-Long.json'        | new OrderCreateResponse(EURUSD,
                new MarketOrderTransaction('6367', '<ACCOUNT>', ldt(2016, JUNE, 22, 13, 41, 29, 264030555), EURUSD, 100),
                null,
                new OrderFillTransaction('6368', '<ACCOUNT>', ldt(2016, JUNE, 22, 13, 41, 29, 264030555), '6367'),
                null
        )

        USDEUR     | 'OrderCreateResponse-Short.json'       | new OrderCreateResponse(USDEUR,
                new MarketOrderTransaction('6367', '<ACCOUNT>', ldt(2016, JUNE, 22, 13, 41, 29, 264030555), USDEUR, 100),
                null,
                new OrderFillTransaction('6368', '<ACCOUNT>', ldt(2016, JUNE, 22, 13, 41, 29, 264030555), '6367'),
                null
        )

        USDEUR     | 'OrderCreateResponse-Canceled.json'    | new OrderCreateResponse(USDEUR,
                new MarketOrderTransaction('1075', '101-001-1775714-001', ldt(2018, SEPTEMBER, 29, 14, 56, 3, 566789846), USDEUR, 1),
                null,
                null,
                new OrderCancelTransaction('1076', '101-001-1775714-001', ldt(2018, SEPTEMBER, 29, 14, 56, 3, 566789846), '1075', MARKET_HALTED, '60495087698774865')
        )

        EURUSD     | 'OrderCreateResponse-Limit-Long.json'  | new OrderCreateResponse(EURUSD,
                null,
                new LimitOrderTransaction('1243', '101-001-1775714-001', ldt(2018, OCTOBER, 12, 10, 7, 41, 517209535), EURUSD, 1, 115600L),
                new OrderFillTransaction('1244', '101-001-1775714-001', ldt(2018, OCTOBER, 12, 10, 7, 41, 517209535), '1243'),
                null
        )

        USDEUR     | 'OrderCreateResponse-Limit-Short.json' | new OrderCreateResponse(USDEUR,
                null,
                new LimitOrderTransaction('1235', '101-001-1775714-001', ldt(2018, OCTOBER, 12, 10, 4, 20, 367755954), USDEUR, 1, 86528),
                new OrderFillTransaction('1236', '101-001-1775714-001', ldt(2018, OCTOBER, 12, 10, 4, 20, 367755954), '1235'),
                null
        )
    }
}
