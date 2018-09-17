package forex.simulator

import forex.broker.AccountID
import forex.broker.Candlestick
import forex.broker.CandlestickData
import forex.broker.InstrumentCandlesRequest
import forex.broker.Price
import forex.broker.PricingGetRequest
import forex.broker.RequestException
import forex.broker.Trade
import forex.broker.TradeListRequest
import forex.broker.TradeState
import forex.broker.TradeSummary
import com.google.common.collect.Range
import forex.market.Instrument
import forex.market.InstrumentHistoryService
import forex.market.MarketEngine
import forex.market.MarketTime
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

import static forex.broker.CandlePrice.ASK
import static forex.broker.CandlePrice.BID
import static forex.broker.CandlePrice.MID
import static forex.broker.CandlestickGranularity.D
import static forex.broker.CandlestickGranularity.M1
import static forex.broker.CandlestickGranularity.W
import static forex.broker.TradeStateFilter.CLOSED
import static java.time.DayOfWeek.FRIDAY
import static java.time.LocalDateTime.now
import static java.time.Month.AUGUST
import static java.time.Month.SEPTEMBER
import static java.util.EnumSet.of
import static forex.market.Instrument.EURUSD
import static forex.market.Instrument.USDEUR
import static forex.market.MarketTime.ZONE

import static java.time.LocalDateTime.of as ldt

class SimulatorContextImplSpec extends Specification {

    static final LocalDateTime now = now()

    def 'should return the correct number of candles for requests with counts'() {

        def now = LocalDateTime.of(2016, SEPTEMBER, 11, 10, 30)
        def start = now.minusWeeks(3)

        InstrumentCandlesRequest request = new InstrumentCandlesRequest(EURUSD);
        request.setPrice(of(BID, MID, ASK));
        request.setGranularity(W);
        request.setFrom(start);
        request.setCount(4);
        request.setIncludeFirst(true);
        request.setWeeklyAlignment(FRIDAY);

        def clock = Mock(MarketTime)
        clock.now() >> now
        clock.getZone() >> ZONE

        def context = new SimulatorContextImpl(clock, new HistoryDataService(clock), Mock(SequenceService),
                Mock(TradeService), Mock(MarketEngine), new SimulatorProperties(pippeteSpread: 20L))

        when: 'weekly candles are requested'
        def response = context.instrumentCandles(request)
        def actual = response.candles

        then: 'the correct candles were returned'
        actual == [
                new Candlestick(ldt(2016, AUGUST, 19, 16, 0), new CandlestickData(113271, 113540, 111796, 111925), new CandlestickData(113291, 113560, 111816, 111945), new CandlestickData(113281, 113550, 111806, 111935)),
                new Candlestick(ldt(2016, AUGUST, 26, 16, 0), new CandlestickData(111924, 112511, 111220, 111570), new CandlestickData(111944, 112531, 111240, 111590), new CandlestickData(111934, 112521, 111230, 111580)),
                new Candlestick(ldt(2016, SEPTEMBER, 2, 16, 0), new CandlestickData(111570, 113259, 111381, 112251), new CandlestickData(111590, 113279, 111401, 112271), new CandlestickData(111580, 113269, 111391, 112261)),
                new Candlestick(ldt(2016, SEPTEMBER, 9, 16, 0), new CandlestickData(112249, 112827, 111484, 111521), new CandlestickData(112269, 112847, 111504, 111541), new CandlestickData(112259, 112837, 111494, 111531))
        ]
    }

    @Unroll
    def 'should prevent invalid requests: #description'() {

        def now = LocalDateTime.of(2016, SEPTEMBER, 11, 10, 30)

        def clock = Mock(MarketTime)
        clock.now() >> now
        clock.getZone() >> ZONE

        def context = new SimulatorContextImpl(clock, new HistoryDataService(clock), Mock(SequenceService),
                Mock(TradeService), Mock(MarketEngine), new SimulatorProperties())

        when: 'an invalid request is specified'
        context.instrumentCandles(request)

        then: 'an exception is thrown'
        thrown RequestException

        where:
        description                   | request
        'specified both to and count' | new InstrumentCandlesRequest(instrument: EURUSD, price: of(BID, MID, ASK),
                granularity: W, from: now.minusWeeks(3), to: LocalDateTime.of(2016, SEPTEMBER, 11, 10, 30),
                includeFirst: true, weeklyAlignment: FRIDAY, count: 4)

        'negative count'              | new InstrumentCandlesRequest(instrument: EURUSD, price: of(BID, MID, ASK),
                granularity: W, from: now.minusWeeks(3), to: now,
                includeFirst: true, weeklyAlignment: FRIDAY, count: -1)

        'from in the future'          | new InstrumentCandlesRequest(instrument: EURUSD, price: of(BID, MID, ASK),
                granularity: W, from: now.plusMinutes(1), to: now.plusMinutes(1),
                includeFirst: true, weeklyAlignment: FRIDAY)
    }

    def 'should return the correct time frames for weekly candle requests'() {

        def end = LocalDateTime.of(2016, SEPTEMBER, 11, 10, 30)
        def start = end.minusWeeks(3)
        Range<LocalDateTime> closed = Range.closed(start, end)

        InstrumentCandlesRequest request = new InstrumentCandlesRequest(EURUSD);
        request.setPrice(of(BID, MID, ASK));
        request.setGranularity(W);
        request.setFrom(closed.lowerEndpoint());
        request.setTo(closed.upperEndpoint());
        request.setIncludeFirst(true);
        request.setWeeklyAlignment(FRIDAY);

        def clock = Mock(MarketTime)
        clock.now() >> end
        clock.getZone() >> ZONE

        def context = new SimulatorContextImpl(clock, new HistoryDataService(clock), Mock(SequenceService),
                Mock(TradeService), Mock(MarketEngine), new SimulatorProperties())

        when: 'weekly candles are requested'
        def response = context.instrumentCandles(request)
        def actual = response.candles.collect { it.time }

        then: 'the candles have the correct time frames'
        actual == [
                LocalDateTime.of(2016, AUGUST, 19, 16, 0),
                LocalDateTime.of(2016, AUGUST, 26, 16, 0),
                LocalDateTime.of(2016, SEPTEMBER, 2, 16, 0),
                LocalDateTime.of(2016, SEPTEMBER, 9, 16, 0)
        ]
    }

    def 'should return the correct time frames for daily candle requests'() {

        def end = LocalDateTime.of(2016, SEPTEMBER, 9, 10, 30)
        def start = end.minusDays(5)
        Range<LocalDateTime> closed = Range.closed(start, end)

        InstrumentCandlesRequest request = new InstrumentCandlesRequest(EURUSD);
        request.setPrice(of(BID, MID, ASK));
        request.setGranularity(D);
        request.setFrom(closed.lowerEndpoint());
        request.setTo(closed.upperEndpoint());
        request.setIncludeFirst(true);

        def clock = Mock(MarketTime)
        clock.now() >> end
        clock.getZone() >> ZONE

        def context = new SimulatorContextImpl(clock, new HistoryDataService(clock), Mock(SequenceService),
                Mock(TradeService), Mock(MarketEngine), new SimulatorProperties())

        when: 'weekly candles are requested'
        def response = context.instrumentCandles(request)
        def actual = response.candles.collect { it.time }

        then: 'the candles have the correct time frames'
        actual == [
                LocalDateTime.of(2016, SEPTEMBER, 4, 16, 0),
                LocalDateTime.of(2016, SEPTEMBER, 5, 16, 0),
                LocalDateTime.of(2016, SEPTEMBER, 6, 16, 0),
                LocalDateTime.of(2016, SEPTEMBER, 7, 16, 0),
                LocalDateTime.of(2016, SEPTEMBER, 8, 16, 0)
        ]
    }

    @Unroll
    def 'should allow requests with a from parameter up to current time: #description'() {

        InstrumentCandlesRequest request = new InstrumentCandlesRequest(EURUSD);
        request.setPrice(of(BID, MID, ASK));
        request.setGranularity(M1);
        request.setFrom(from);
        request.setTo(to);
        request.setIncludeFirst(true);

        def clock = Mock(MarketTime)
        clock.now() >> LocalDateTime.of(2016, SEPTEMBER, 9, 10, 30)
        clock.getZone() >> ZONE

        def context = new SimulatorContextImpl(clock, new HistoryDataService(clock), Mock(SequenceService),
                Mock(TradeService), Mock(MarketEngine), new SimulatorProperties())

        when: 'candles are requested'
        context.instrumentCandles(request)

        then: 'an exception was thrown for invalid dates'
        notThrown(Exception)

        where:
        description                              | from                                         | to
        'requesting previous and current minute' | LocalDateTime.of(2016, SEPTEMBER, 9, 10, 29) | LocalDateTime.of(2016, SEPTEMBER, 9, 10, 30)
        'requesting current minute'              | LocalDateTime.of(2016, SEPTEMBER, 9, 10, 30) | LocalDateTime.of(2016, SEPTEMBER, 9, 10, 30)
    }

    @Shared
    def tradeHistories = [new TradeHistory(new TradeSummary('99', EURUSD, 116058, now.minusHours(3), 2, 0, -15L, 0L, now.minusHours(2).minusMinutes(3)), candles([(now.minusHours(2).minusMinutes(3)): new CandlestickData(116050L, 116070L, 116040L, 116040L)])),
                          new TradeHistory(new TradeSummary('100', EURUSD, 116058, now.minusHours(2), 2, 0, -15L, 0L, now.minusHours(1).minusMinutes(2)), candles([(now.minusHours(1).minusMinutes(2)): new CandlestickData(116050L, 116070L, 116040L, 116040L)])),
                          new TradeHistory(new TradeSummary('101', EURUSD, 116028, now.minusHours(1), 2, 0, 25L, 0L, now), candles([(now): new CandlestickData(116040L, 116060L, 116030L, 116030L)]))]

    @Unroll
    def 'should return the correct trades based on filter in descending time'() {

        def clock = Mock(MarketTime)
        def tradeService = Mock(TradeService)
        def context = new SimulatorContextImpl(clock, Mock(InstrumentHistoryService), Mock(SequenceService), tradeService,
                Mock(MarketEngine), new SimulatorProperties())

        when: 'the trade request is made'
        def actual = context.trade().list(request).trades

        then:
        actual == expected

        and: 'closed trades were retrieved'
        1 * tradeService.getClosedTradesForAccountID(request.getAccountID().id) >> {
            def result = new TreeSet<>(new Comparator<TradeHistory>() {
                @Override
                int compare(TradeHistory o1, TradeHistory o2) {
                    return o1.getOpenTime().compareTo(o2.getOpenTime())
                }
            })
            result.addAll(closedTrades)
            return result
        }

        where:
        request                                              | expected                                                                                                                                         | closedTrades
        new TradeListRequest(new AccountID('1'), CLOSED, 1)  | [new Trade('101', EURUSD, 116028, now.minusHours(1), TradeState.CLOSED, 2, 0, 25L, 0L, 0L, 116040L, [], 0L, now)]                                | tradeHistories

        new TradeListRequest(new AccountID('1'), CLOSED, 2)  | [new Trade('101', EURUSD, 116028, now.minusHours(1), TradeState.CLOSED, 2, 0, 25L, 0L, 0L, 116040L, [], 0L, now),
                                                                new Trade('100', EURUSD, 116058, now.minusHours(2), TradeState.CLOSED, 2, 0, -15L, 0L, 0L, 116050L, [], 0L, now.minusHours(1).minusMinutes(2))] | tradeHistories

        new TradeListRequest(new AccountID('1'), CLOSED, 50) | [new Trade('101', EURUSD, 116028, now.minusHours(1), TradeState.CLOSED, 2, 0, 25L, 0L, 0L, 116040L, [], 0L, now),
                                                                new Trade('100', EURUSD, 116058, now.minusHours(2), TradeState.CLOSED, 2, 0, -15L, 0L, 0L, 116050L, [], 0L, now.minusHours(1).minusMinutes(2)),
                                                                new Trade('99', EURUSD, 116058, now.minusHours(3), TradeState.CLOSED, 2, 0, -15L, 0L, 0L, 116050L, [], 0L, now.minusHours(2).minusMinutes(3))]  | tradeHistories
    }

    static NavigableMap<LocalDateTime, CandlestickData> candles(Map<LocalDateTime, CandlestickData> map) {
        NavigableMap<LocalDateTime, CandlestickData> retVal = new TreeMap<>();
        retVal.putAll(map);
        return retVal
    }

    @Unroll
    def 'should reject trade list requests for > 500: #rejected'() {

        def clock = Mock(MarketTime)

        def tradeService = Mock(TradeService)
        tradeService.getClosedTradesForAccountID(_) >> new TreeSet<>()

        def context = new SimulatorContextImpl(clock, Mock(InstrumentHistoryService), Mock(SequenceService), tradeService,
                Mock(MarketEngine), new SimulatorProperties())

        when: 'the trade request is made'
        def thrown = null
        try {
            context.trade().list(new TradeListRequest(new AccountID('1'), CLOSED, count)).trades
        } catch (Exception e) {
            thrown = e
        }

        then: 'request for > 500 trades were rejected'
        rejected == (thrown != null)

        where:
        count | rejected
        501   | true
        500   | false
    }

    @Unroll
    def 'should return prices for all requested instruments with spread applied: #instruments'() {

        def clock = Mock(MarketTime)
        def marketEngine = Mock(MarketEngine)

        def context = new SimulatorContextImpl(clock, Mock(InstrumentHistoryService), Mock(SequenceService),
                Mock(TradeService), marketEngine, new SimulatorProperties(pippeteSpread: 20L))

        when: 'the price request is made'
        def response = context.pricing().get(new PricingGetRequest(new AccountID('1'), instruments as Set))
        def actual = response.prices

        then: 'the correct prices were returned for all instruments'
        actual == expected

        and: 'the market engine gave the price'
        instruments.size() * marketEngine.getPrice(_ as Instrument) >> { it ->
            return it[0] == EURUSD ? 112345L : 54321L
        }

        where:
        instruments      | expected
        [EURUSD]         | [new Price(EURUSD, 112335L, 112355L)]
        [EURUSD, USDEUR] | [new Price(EURUSD, 112335L, 112355L), new Price(USDEUR, 54311L, 54331L)]
    }
}
