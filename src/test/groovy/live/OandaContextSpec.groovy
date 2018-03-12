package live

import broker.Price
import broker.PricingGetResponse
import com.oanda.v20.pricing.PriceBucket
import com.oanda.v20.pricing.PriceValue
import com.oanda.v20.primitives.DateTime
import com.oanda.v20.primitives.InstrumentName
import spock.lang.Specification
import spock.lang.Unroll

import static market.Instrument.EURUSD
import static market.Instrument.USDEUR

class OandaContextSpec extends Specification {

    @Unroll
    def 'should convert pricing responses correctly: #instrument'() {

        com.oanda.v20.pricing.PricingGetResponse oandaResponse = new com.oanda.v20.pricing.PricingGetResponse()
        oandaResponse.time = new DateTime("2018-03-12T00:32:59.062908651Z")
        oandaResponse.prices = [new com.oanda.v20.pricing.Price(
                instrument: new InstrumentName(EURUSD.symbol),
                bids: [new PriceBucket(price: new PriceValue("1.23065"))],
                asks: [new PriceBucket(price: new PriceValue("1.23109"))],
                closeoutBid: new PriceValue("1.23062"),
                closeoutAsk: new PriceValue("1.23113"))]

        def actual = OandaContext.convert([instrument] as Set, oandaResponse)

        expect:
        actual == new PricingGetResponse([
                new Price(instrument, expectedBid, expectedAsk)
        ])

        where:
        instrument | expectedBid | expectedAsk
        USDEUR     | 81229L      | 81258L
        EURUSD     | 123065L     | 123109L
    }
}
