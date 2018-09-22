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

            HighLowProjection highLow = repo.findHighLow(candle, maxTimeToUse);

            InstrumentCandleType id = new InstrumentCandleType();
            id.setInstrument(instrument);
            id.setGranularity(M1);
            id.setTime(candle);

            Optional<InstrumentCandle> start = repo.findById(id);
            while (!start.isPresent() && id.getTime().isBefore(maxTimeToUse)) {
                id.setTime(id.getTime().plusMinutes(1));
                start = repo.findById(id);
            }
            InstrumentCandle open = start.get();

            id.setTime(maxTimeToUse.minusMinutes(1));
            Optional<InstrumentCandle> end = repo.findById(id);
            InstrumentCandle close = end.get();

            result.put(candle, new CandlestickData(open.getMidOpen(), highLow.getHigh(), highLow.getLow(), close.getMidClose()));

            candle = nextCandle;
            nextCandle = timeFrame.nextCandle(candle);
        }

        return result;
    }
}
