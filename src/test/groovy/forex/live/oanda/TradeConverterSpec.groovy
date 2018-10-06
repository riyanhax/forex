package forex.live.oanda

import com.google.common.collect.ImmutableSet
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oanda.v20.order.OrderAdapter
import com.oanda.v20.trade.TradeStateFilter
import com.oanda.v20.transaction.TransactionAdapter
import forex.broker.MarketOrderTransaction
import forex.broker.Trade
import forex.broker.TradeCloseResponse
import forex.broker.TradeListRequest
import forex.broker.TradeListResponse
import forex.broker.TradeState
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.Month

import static forex.broker.TradeStateFilter.ALL
import static forex.broker.TradeStateFilter.CLOSED
import static forex.broker.TradeStateFilter.CLOSE_WHEN_TRADEABLE
import static forex.broker.TradeStateFilter.OPEN
import static forex.market.Instrument.EURUSD
import static forex.market.Instrument.USDEUR

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
                '101-001-1775714-001',
                LocalDateTime.of(2018, Month.SEPTEMBER, 7, 10, 56, 46, 371386767), EURUSD, 7))

        USDEUR     | 'TradeCloseResponse-Short.json' | new TradeCloseResponse(new MarketOrderTransaction('1006',
                '101-001-1775714-001',
                LocalDateTime.of(2018, Month.SEPTEMBER, 7, 10, 56, 46, 371386767), USDEUR, 7))
    }

    def 'should convert trade list response correctly'() {

        def accountID = '1'
        def expected = new TradeListResponse([
                // Oanda Short -> Inverse Long
                new Trade('309', accountID, USDEUR, 86233L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 7, 43, 13, 567036542), TradeState.CLOSED, 1, 0, 100L, 0L, 0L, 86308L, ['312'], 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 07, 45, 11, 338759441)),
                new Trade('303', accountID, EURUSD, 115879L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 7, 31, 9, 524922739), TradeState.CLOSED, 2, 0, 150L, 0L, 0L, 115955L, ['306'], 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 07, 42, 34, 554252280)),
                new Trade('297', accountID, EURUSD, 116009L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 7, 30, 8, 872381160), TradeState.CLOSED, 1, 0, -140L, 0L, 0L, 115872L, ['300'], 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 07, 30, 31, 910644106)),
                new Trade('291', accountID, EURUSD, 116085L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 6, 27, 48, 889977095), TradeState.CLOSED, 2, 0, 170L, 0L, 0L, 116171L, ['294'], 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 07, 29, 19, 743932770)),
                new Trade('285', accountID, EURUSD, 116142L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 6, 8, 42, 577999259), TradeState.CLOSED, 1, 0, -120L, 0L, 0L, 116026L, ['288'], 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 06, 27, 15, 746691505)),
                // Oanda Short -> Inverse Long
                new Trade('279', accountID, USDEUR, 85989L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 5, 32, 30, 591143608), TradeState.CLOSED, 16, 0, 1650L, 0L, 0L, 86065L, ['282'], 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 06, 8, 19, 414617653)),
                new Trade('273', accountID, EURUSD, 116357L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 4, 56, 17, 566296312), TradeState.CLOSED, 8, 0, -900L, 0L, 0L, 116244L, ['276'], 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 05, 31, 40, 471536562)),
                new Trade('267', accountID, EURUSD, 116471L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 2, 57, 41, 923980261), TradeState.CLOSED, 4, 0, -460L, 0L, 0L, 116355L, ['270'], 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 04, 55, 37, 810097632)),
                // Oanda Short -> Inverse Long
                new Trade('261', accountID, USDEUR, 85938L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 2, 10, 27, 208913438), TradeState.CLOSED, 2, 0, -200L, 0L, 0L, 85865L, ['264'], 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 02, 57, 26, 363855494)),
                new Trade('255', accountID, EURUSD, 116478L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 2, 01, 24, 199637311), TradeState.CLOSED, 1, 0, -110L, 0L, 0L, 116364L, ['258'], 0L, LocalDateTime.of(2018, Month.SEPTEMBER, 7, 02, 10, 00, 438812631))
        ], "317")

        def json = getClass().getResourceAsStream('TradeListResponse.json').text
        def response = gson.fromJson(json, com.oanda.v20.trade.TradeListResponse.class)

        TradeListResponse actual = TradeConverter.convert(response, accountID)

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

    @Unroll
    def 'should convert trade list request to oanda version correctly'() {

        def actual = gson.toJson(TradeConverter.convert(request))

        expect:
        actual == expected

        where:
        request                                                             | expected
        new TradeListRequest('1234', CLOSED, 2)                             | '''{
  "pathParams": {
    "accountID": "1234"
  },
  "queryParams": {
    "count": 2,
    "state": "CLOSED"
  }
}'''

        new TradeListRequest('1234', OPEN, 3)                               | '''{
  "pathParams": {
    "accountID": "1234"
  },
  "queryParams": {
    "count": 3,
    "state": "OPEN"
  }
}'''

        new TradeListRequest('1234', CLOSED, ImmutableSet.of('102', '104')) | '''{
  "pathParams": {
    "accountID": "1234"
  },
  "queryParams": {
    "count": 50,
    "ids": [
      "102",
      "104"
    ],
    "state": "CLOSED"
  }
}'''
    }
}
