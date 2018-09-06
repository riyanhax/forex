package live

import broker.Price
import broker.PricingGetResponse
import broker.TradeSummary
import com.oanda.v20.pricing.PriceBucket
import com.oanda.v20.pricing.PriceValue
import com.oanda.v20.primitives.DateTime
import com.oanda.v20.primitives.InstrumentName
import com.oanda.v20.trade.Trade
import com.oanda.v20.trade.TradeID
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.Month

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

    @Unroll
    def 'should convert trades correctly'() {

        def actual = OandaContext.convert(oandaTrade)

        expect:
        actual.equals(expected)

        where:
        oandaTrade                                         | expected
        // Oanda Long -> Long
        new Trade(id: new TradeID('199'), instrument: new InstrumentName('EUR_USD'), price: new PriceValue(1.16237d),
                openTime: '2018-09-06T20:00:11.0Z', state: 'CLOSED', initialUnits: 128, currentUnits: 0, realizedPL: 0.0154d, marginUsed: null,
                averageClosePrice: 1.16249d, closingTransactionIDs: ['203'], financing: -0.0002d, closeTime: '2018-09-06T20:27:30.0Z', clientExtensions: null
                /*, takeProfitOrder:, /*stopLossOrder: */) |

                new TradeSummary(EURUSD, 128, 116237, 1540,
                        0, LocalDateTime.of(2018, Month.SEPTEMBER, 6, 15, 0, 11),
                        LocalDateTime.of(2018, Month.SEPTEMBER, 6, 15, 27, 30), '199')

        // Oanda Short -> Inverse Long
        new Trade(id: new TradeID('192'), instrument: new InstrumentName('EUR_USD'), price: new PriceValue(1.16305d),
                openTime: '2018-09-06T18:30:42.0Z', state: 'CLOSED', initialUnits: -128, currentUnits: 0, realizedPL: 0.1306d, marginUsed: null,
                averageClosePrice: 1.16203d, closingTransactionIDs: ['196'], financing: 0.0003d, closeTime: '2018-09-06T20:00:00.0Z', clientExtensions: null
                /*, takeProfitOrder:, /*stopLossOrder: */) |

                new TradeSummary(USDEUR, 128, 85980, 13060,
                        0, LocalDateTime.of(2018, Month.SEPTEMBER, 6, 13, 30, 42),
                        LocalDateTime.of(2018, Month.SEPTEMBER, 6, 15, 0, 0), '192')
    }
}
