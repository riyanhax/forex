package simulator

import broker.InstrumentCandlesRequest
import com.google.common.collect.Range
import market.MarketEngine
import market.MarketTime
import spock.lang.Specification

import java.time.LocalDateTime

import static broker.CandlePrice.ASK
import static broker.CandlePrice.BID
import static broker.CandlePrice.MID
import static broker.CandlestickGranularity.D
import static broker.CandlestickGranularity.W
import static java.time.DayOfWeek.FRIDAY
import static java.time.Month.AUGUST
import static java.time.Month.SEPTEMBER
import static java.util.EnumSet.of
import static market.Instrument.EURUSD
import static market.MarketTime.ZONE

class SimulatorContextImplSpec extends Specification {

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
                Mock(MarketEngine), new SimulatorProperties())

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
                Mock(MarketEngine), new SimulatorProperties())

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

}
