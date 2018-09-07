package live.oanda

import broker.Price
import broker.PricingGetResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oanda.v20.order.OrderAdapter
import com.oanda.v20.transaction.TransactionAdapter
import spock.lang.Specification
import spock.lang.Unroll

import static market.Instrument.EURUSD
import static market.Instrument.USDEUR

class PricingConverterSpec extends Specification {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(com.oanda.v20.order.Order.class, new OrderAdapter())
            .registerTypeAdapter(com.oanda.v20.transaction.Transaction.class, new TransactionAdapter())
            .create();

    @Unroll
    def 'should convert pricing responses correctly: #instrument'() {

        def expected = new PricingGetResponse([
                new Price(instrument, expectedBid, expectedAsk)
        ])

        def json = getClass().getResourceAsStream('PricingGetResponse.json').text
        def response = gson.fromJson(json, com.oanda.v20.pricing.PricingGetResponse.class)

        PricingGetResponse actual = PricingConverter.convert([instrument] as Set, response)

        expect:
        actual == expected

        where:
        instrument | expectedBid | expectedAsk
        USDEUR     | 86365L      | 86374L
        EURUSD     | 115775L     | 115787L
    }

}
