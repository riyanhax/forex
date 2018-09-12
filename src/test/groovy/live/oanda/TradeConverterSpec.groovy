package live.oanda

import broker.MarketOrderTransaction
import broker.TradeCloseResponse
import broker.TradeListResponse
import broker.TradeSummary
import broker.TransactionID
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oanda.v20.order.OrderAdapter
import com.oanda.v20.trade.TradeStateFilter
import com.oanda.v20.transaction.TransactionAdapter
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.Month

import static broker.TradeStateFilter.ALL
import static broker.TradeStateFilter.CLOSED
import static broker.TradeStateFilter.CLOSE_WHEN_TRADEABLE
import static broker.TradeStateFilter.OPEN
import static market.Instrument.EURUSD
import static market.Instrument.USDEUR

class TradeConverterSpec extends Specification {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(com.oanda.v20.order.Order.class, new OrderAdapter())
            .registerTypeAdapter(com.oanda.v20.transaction.Transaction.class, new TransactionAdapter())
            .create();

    @Unroll
    def 'should convert trade close response correctly: #instrument'() {

        def json = getClass().getResourceAsStream(responseFile).text
        def response = gson.fromJson(json, com.oanda.v20.trade.TradeCloseResponse.class)

        TradeCloseResponse actual = TradeConverter.convert(response)

        expect:
        actual == expected

        where:
        instrument | responseFile                    | expected
        EURUSD     | 'TradeCloseResponse-Long.json'  | new TradeCloseResponse(new MarketOrderTransaction('1006',
                LocalDateTime.of(2018, Month.SEPTEMBER, 7, 10, 56, 46, 371386767), EURUSD, 7))

        USDEUR     | 'TradeCloseResponse-Short.json' | new TradeCloseResponse(new MarketOrderTransaction('1006',
                LocalDateTime.of(2018, Month.SEPTEMBER, 7, 10, 56, 46, 371386767), USDEUR, 7))
    }

    def 'should convert trade list response correctly'() {

        def expected = new TradeListResponse([
                // Oanda Short -> Inverse Long
                new TradeSummary(USDEUR, 1, 86233L, 100L, 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 7, 43, 13, 567036542), LocalDateTime.of(2018, Month.SEPTEMBER, 7, 07, 45, 11, 338759441), '309'),
                new TradeSummary(EURUSD, 2, 115879L, 150L, 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 7, 31, 9, 524922739), LocalDateTime.of(2018, Month.SEPTEMBER, 7, 07, 42, 34, 554252280), '303'),
                new TradeSummary(EURUSD, 1, 116009L, -140L, 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 7, 30, 8, 872381160), LocalDateTime.of(2018, Month.SEPTEMBER, 7, 07, 30, 31, 910644106), '297'),
                new TradeSummary(EURUSD, 2, 116085L, 170L, 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 6, 27, 48, 889977095), LocalDateTime.of(2018, Month.SEPTEMBER, 7, 07, 29, 19, 743932770), '291'),
                new TradeSummary(EURUSD, 1, 116142L, -120L, 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 6, 8, 42, 577999259), LocalDateTime.of(2018, Month.SEPTEMBER, 7, 06, 27, 15, 746691505), '285'),
                // Oanda Short -> Inverse Long
                new TradeSummary(USDEUR, 16, 85989L, 1650L, 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 5, 32, 30, 591143608), LocalDateTime.of(2018, Month.SEPTEMBER, 7, 06, 8, 19, 414617653), '279'),
                new TradeSummary(EURUSD, 8, 116357L, -900L, 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 4, 56, 17, 566296312), LocalDateTime.of(2018, Month.SEPTEMBER, 7, 05, 31, 40, 471536562), '273'),
                new TradeSummary(EURUSD, 4, 116471L, -460L, 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 2, 57, 41, 923980261), LocalDateTime.of(2018, Month.SEPTEMBER, 7, 04, 55, 37, 810097632), '267'),
                // Oanda Short -> Inverse Long
                new TradeSummary(USDEUR, 2, 85938L, -200L, 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 2, 10, 27, 208913438), LocalDateTime.of(2018, Month.SEPTEMBER, 7, 02, 57, 26, 363855494), '261'),
                new TradeSummary(EURUSD, 1, 116478L, -110L, 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 2, 01, 24, 199637311), LocalDateTime.of(2018, Month.SEPTEMBER, 7, 02, 10, 00, 438812631), '255')
        ], new TransactionID("317"))

        def json = getClass().getResourceAsStream('TradeListResponse.json').text
        def response = gson.fromJson(json, com.oanda.v20.trade.TradeListResponse.class)

        TradeListResponse actual = TradeConverter.convert(response)

        expect:
        actual == expected
    }

    @Unroll
    def 'should convert trade state filter to oanda version correctly: #filter'() {

        def actual = TradeConverter.convert(filter)

        expect:
        actual == expected

        where:
        filter               | expected
        OPEN                 | TradeStateFilter.OPEN
        CLOSED               | TradeStateFilter.CLOSED
        CLOSE_WHEN_TRADEABLE | TradeStateFilter.CLOSE_WHEN_TRADEABLE
        ALL                  | TradeStateFilter.ALL
    }
}
