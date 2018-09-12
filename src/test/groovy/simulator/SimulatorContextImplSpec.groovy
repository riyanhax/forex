package simulator

import broker.AccountID
import broker.InstrumentCandlesRequest
import broker.TradeListRequest
import broker.TradeSummary
import com.google.common.collect.Range
import market.InstrumentHistoryService
import market.MarketEngine
import market.MarketTime
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

import static broker.CandlePrice.ASK
import static broker.CandlePrice.BID
import static broker.CandlePrice.MID
import static broker.CandlestickGranularity.D
import static broker.CandlestickGranularity.W
import static broker.TradeStateFilter.CLOSED
import static java.time.DayOfWeek.FRIDAY
import static java.time.LocalDateTime.now
import static java.time.Month.AUGUST
import static java.time.Month.SEPTEMBER
import static java.util.EnumSet.of
import static market.Instrument.EURUSD
import static market.MarketTime.ZONE

class SimulatorContextImplSpec extends Specification {

    static final LocalDateTime now = now()

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
        request                                              | expected                                                                                                     | closedTrades
        new TradeListRequest(new AccountID('1'), CLOSED, 1)  | [new TradeSummary(EURUSD, 2, 116028, 0L, 25L, now.minusHours(1), now, '101')]                                | [new TradeHistory(new TradeSummary(EURUSD, 2, 116058, 0L, -15L, now.minusHours(3), now.minusHours(2).minusMinutes(3), '99'), [:] as NavigableMap),
                                                                                                                                                                               new TradeHistory(new TradeSummary(EURUSD, 2, 116058, 0L, -15L, now.minusHours(2), now.minusHours(1).minusMinutes(2), '100'), [:] as NavigableMap),
                                                                                                                                                                               new TradeHistory(new TradeSummary(EURUSD, 2, 116028, 0L, 25L, now.minusHours(1), now, '101'), [:] as NavigableMap)]

        new TradeListRequest(new AccountID('1'), CLOSED, 2)  | [new TradeSummary(EURUSD, 2, 116028, 0L, 25L, now.minusHours(1), now, '101'),
                                                                new TradeSummary(EURUSD, 2, 116058, 0L, -15L, now.minusHours(2), now.minusHours(1).minusMinutes(2), '100')] | [new TradeHistory(new TradeSummary(EURUSD, 2, 116058, 0L, -15L, now.minusHours(3), now.minusHours(2).minusMinutes(3), '99'), [:] as NavigableMap),
                                                                                                                                                                               new TradeHistory(new TradeSummary(EURUSD, 2, 116058, 0L, -15L, now.minusHours(2), now.minusHours(1).minusMinutes(2), '100'), [:] as NavigableMap),
                                                                                                                                                                               new TradeHistory(new TradeSummary(EURUSD, 2, 116028, 0L, 25L, now.minusHours(1), now, '101'), [:] as NavigableMap)]

        new TradeListRequest(new AccountID('1'), CLOSED, 50) | [new TradeSummary(EURUSD, 2, 116028, 0L, 25L, now.minusHours(1), now, '101'),
                                                                new TradeSummary(EURUSD, 2, 116058, 0L, -15L, now.minusHours(2), now.minusHours(1).minusMinutes(2), '100'),
                                                                new TradeSummary(EURUSD, 2, 116058, 0L, -15L, now.minusHours(3), now.minusHours(2).minusMinutes(3), '99')]  | [new TradeHistory(new TradeSummary(EURUSD, 2, 116058, 0L, -15L, now.minusHours(3), now.minusHours(2).minusMinutes(3), '99'), [:] as NavigableMap),
                                                                                                                                                                               new TradeHistory(new TradeSummary(EURUSD, 2, 116058, 0L, -15L, now.minusHours(2), now.minusHours(1).minusMinutes(2), '100'), [:] as NavigableMap),
                                                                                                                                                                               new TradeHistory(new TradeSummary(EURUSD, 2, 116028, 0L, 25L, now.minusHours(1), now, '101'), [:] as NavigableMap)]
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
}
