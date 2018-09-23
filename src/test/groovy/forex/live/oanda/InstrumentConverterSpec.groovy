package forex.live.oanda

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oanda.v20.order.OrderAdapter
import com.oanda.v20.transaction.TransactionAdapter
import forex.broker.Candlestick
import forex.broker.CandlestickData
import forex.broker.InstrumentCandlesResponse
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

import static forex.broker.CandlestickGranularity.W
import static forex.market.Instrument.EURUSD
import static forex.market.Instrument.USDEUR
import static java.time.Month.AUGUST

class InstrumentConverterSpec extends Specification {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(com.oanda.v20.order.Order.class, new OrderAdapter())
            .registerTypeAdapter(com.oanda.v20.transaction.Transaction.class, new TransactionAdapter())
            .create();

    @Unroll
    def 'should convert instrument candle response correctly: #instrument'() {

        def json = getClass().getResourceAsStream('InstrumentCandlesResponse.json').text
        def response = gson.fromJson(json, com.oanda.v20.instrument.InstrumentCandlesResponse.class)

        InstrumentCandlesResponse actual = InstrumentConverter.convert(instrument, response)

        expect:
        actual == expected

        where:
        instrument | expected
        EURUSD     | new InstrumentCandlesResponse(EURUSD, W, [
                new Candlestick(LocalDateTime.of(2018, AUGUST, 10, 16, 0),
                        new CandlestickData(113685, 114443, 113003, 114346),
                        new CandlestickData(113745, 114456, 113016, 114406),
                        new CandlestickData(113715, 114450, 113010, 114376)),
                new Candlestick(LocalDateTime.of(2018, AUGUST, 17, 16, 0),
                        new CandlestickData(114365, 116393, 113938, 116190),
                        new CandlestickData(114425, 116412, 113953, 116250),
                        new CandlestickData(114395, 116402, 113946, 116220)),
                new Candlestick(LocalDateTime.of(2018, AUGUST, 24, 16, 0),
                        new CandlestickData(116126, 117330, 115836, 115989),
                        new CandlestickData(116181, 117345, 115851, 116049),
                        new CandlestickData(116154, 117337, 115844, 116019)),
                new Candlestick(LocalDateTime.of(2018, AUGUST, 31, 16, 0),
                        new CandlestickData(115955, 116587, 115296, 115738),
                        new CandlestickData(116010, 116602, 115309, 115749),
                        new CandlestickData(115982, 116594, 115302, 115744))])

        USDEUR     | new InstrumentCandlesResponse(USDEUR, W, [
                new Candlestick(LocalDateTime.of(2018, AUGUST, 10, 16, 0),
                        new CandlestickData(87916L, 88483L, 87370L, 87408L),
                        new CandlestickData(87962L, 88493L, 87380L, 87454L),
                        new CandlestickData(87939L, 88488L, 87374L, 87431L)),
                new Candlestick(LocalDateTime.of(2018, AUGUST, 17, 16, 0),
                        new CandlestickData(87393L, 87755L, 85902L, 86022L),
                        new CandlestickData(87439L, 87767L, 85916L, 86066L),
                        new CandlestickData(87416L, 87761L, 85909L, 86044L)),

                new Candlestick(LocalDateTime.of(2018, AUGUST, 24, 16, 0),
                        new CandlestickData(86073L, 86318L, 85219L, 86170L),
                        new CandlestickData(86113L, 86329L, 85230L, 86215L),
                        new CandlestickData(86093L, 86323L, 85225L, 86193L)),
                new Candlestick(LocalDateTime.of(2018, AUGUST, 31, 16, 0),
                        new CandlestickData(86199L, 86723L, 85762L, 86394L),
                        new CandlestickData(86240L, 86733L, 85773L, 86402L),
                        new CandlestickData(86220L, 86729L, 85768L, 86398L))])
    }
}
