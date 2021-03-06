package forex.market;

import com.google.common.collect.Range;
import forex.broker.CandlestickData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static forex.broker.CandlestickData.inverse;
import static forex.broker.CandlestickGranularity.M1;

public class DBHistoryDataService implements InstrumentHistoryService {

    private final MarketTime clock;
    private final InstrumentCandleRepository repo;

    public DBHistoryDataService(MarketTime clock, InstrumentCandleRepository repo) {
        this.clock = clock;
        this.repo = repo;
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getOneDayCandles(Instrument instrument, Range<LocalDateTime> inclusiveRange) {
        return getOHLC(CandleTimeFrame.FOUR_HOURS, instrument, inclusiveRange);
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getFourHourCandles(Instrument instrument, Range<LocalDateTime> inclusiveRange) {
        return getOHLC(CandleTimeFrame.FOUR_HOURS, instrument, inclusiveRange);
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getFiveMinuteCandles(Instrument instrument, Range<LocalDateTime> inclusiveRange) {
        return getOHLC(CandleTimeFrame.FIVE_MINUTE, instrument, inclusiveRange);
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getOneMinuteCandles(Instrument instrument, Range<LocalDateTime> inclusiveRange) {
        return getOHLC(CandleTimeFrame.ONE_MINUTE, instrument, inclusiveRange);
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getOneWeekCandles(Instrument instrument, Range<LocalDateTime> inclusiveRange) {
        return getOHLC(CandleTimeFrame.ONE_WEEK, instrument, inclusiveRange);
    }

    @Override
    public Optional<InstrumentHistory> getData(Instrument instrument, LocalDateTime time) {
        NavigableMap<LocalDateTime, CandlestickData> oneMinuteCandles = getOneMinuteCandles(instrument, Range.closed(time, time));

        return oneMinuteCandles.isEmpty() ? Optional.empty() : Optional.of(new InstrumentHistory(instrument, time, oneMinuteCandles.get(time)));
    }

    @Override
    public Set<LocalDate> getAvailableDays(Instrument instrument, int year) {
        return repo.findAll().stream()
                .map(it -> it.getId().getTime().toLocalDate())
                .collect(Collectors.toSet());
    }

    NavigableMap<LocalDateTime, CandlestickData> getOHLC(CandleTimeFrame timeFrame, Instrument instrument, Range<LocalDateTime> between) {

        TreeMap<LocalDateTime, CandlestickData> result = new TreeMap<>();

        Instrument brokerInstrument = instrument.getBrokerInstrument();
        boolean inverse = instrument.isInverse();

        LocalDateTime candle = timeFrame.calculateStart(between.lowerEndpoint());
        LocalDateTime nextCandle = timeFrame.nextCandle(candle);

        LocalDateTime now = clock.now();
        LocalDateTime currentCandleStart = timeFrame.calculateStart(now);

        LocalDateTime lastCandleStart = timeFrame.calculateStart(between.upperEndpoint());
        if (lastCandleStart.isAfter(currentCandleStart)) {
            lastCandleStart = currentCandleStart;
        }

        while (!candle.isAfter(lastCandleStart)) {

            LocalDateTime maxTimeToUse = nextCandle;
            if (maxTimeToUse.isAfter(now)) {
                maxTimeToUse = now;
            }

            OhlcProjection highLow = repo.findOhlc(brokerInstrument, candle, maxTimeToUse);

            InstrumentCandleType id = new InstrumentCandleType();
            id.setInstrument(brokerInstrument);
            id.setGranularity(M1);
            id.setTime(highLow.getOpen());

            Optional<InstrumentCandle> start = repo.findById(id);
            if (!start.isPresent()) {
                candle = nextCandle;
                nextCandle = timeFrame.nextCandle(candle);

                continue;
            }
            InstrumentCandle open = start.get();

            id.setTime(highLow.getClose());

            Optional<InstrumentCandle> end = repo.findById(id);
            if (!end.isPresent()) {
                candle = nextCandle;
                nextCandle = timeFrame.nextCandle(candle);

                continue;
            }
            InstrumentCandle close = end.get();

            CandlestickData candlestickData = new CandlestickData(open.getMidOpen(), highLow.getHigh(), highLow.getLow(), close.getMidClose());
            if (inverse) {
                candlestickData = inverse(candlestickData);
            }
            result.put(candle, candlestickData);

            candle = nextCandle;
            nextCandle = timeFrame.nextCandle(candle);
        }

        return result;
    }
}
