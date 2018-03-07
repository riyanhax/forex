package simulator;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;
import market.CandleTimeFrame;
import market.CurrencyPairHistory;
import market.CurrencyPairHistoryService;
import market.Instrument;
import market.MarketTime;
import market.OHLC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static broker.Quote.pippetesFromDouble;
import static market.CandleTimeFrame.FIFTEEN_MINUTE;
import static market.CandleTimeFrame.FIVE_MINUTE;
import static market.CandleTimeFrame.FOUR_HOURS;
import static market.CandleTimeFrame.ONE_DAY;
import static market.CandleTimeFrame.ONE_HOUR;
import static market.CandleTimeFrame.ONE_MINUTE;
import static market.CandleTimeFrame.ONE_MONTH;
import static market.CandleTimeFrame.ONE_WEEK;
import static market.CandleTimeFrame.THIRTY_MINUTE;

@Service
class HistoryDataCurrencyPairService implements CurrencyPairHistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryDataCurrencyPairService.class);

    private static class CurrencyPairYear {
        final Instrument pair;
        final int year;

        private CurrencyPairYear(Instrument pair, int year) {
            this.pair = pair;
            this.year = year;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CurrencyPairYear that = (CurrencyPairYear) o;
            return year == that.year &&
                    pair == that.pair;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pair, year);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("instrument", pair)
                    .add("year", year)
                    .toString();
        }
    }

    private static class CurrencyData {
        final CandleTimeFrame timeFrame;
        final NavigableMap<LocalDateTime, OHLC> ohlcData;
        final NavigableSet<LocalDate> availableDates;

        public CurrencyData(CandleTimeFrame timeFrame, NavigableMap<LocalDateTime, OHLC> ohlcData, NavigableSet<LocalDate> availableDates) {
            this.timeFrame = timeFrame;
            this.ohlcData = ohlcData;
            this.availableDates = availableDates;
        }
    }

    private final DateTimeFormatter timestampParser = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
    private final LoadingCache<CurrencyPairYear, CurrencyData> minuteCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<CurrencyPairYear, CurrencyData>() {
                @Override
                public CurrencyData load(CurrencyPairYear pairYear) throws Exception {

                    Stopwatch timer = Stopwatch.createStarted();

                    String path = String.format("/history/DAT_ASCII_%s_M1_%d.csv", pairYear.pair.name(), pairYear.year);
                    try (InputStreamReader is = new InputStreamReader(HistoryDataCurrencyPairService.class.getResourceAsStream(path))) {

                        NavigableMap<LocalDateTime, OHLC> result = CharStreams.readLines(is, new LineProcessor<NavigableMap<LocalDateTime, OHLC>>() {
                            NavigableMap<LocalDateTime, OHLC> values = new TreeMap<>();

                            @Override
                            public boolean processLine(String line) throws IOException {
                                int part = 0;
                                String[] parts = line.split(";");
                                LocalDateTime parsedDate = LocalDateTime.parse(parts[part++], timestampParser).withSecond(0);
                                // The files are at UTC-5 (no daylight savings timestamp observed)
                                OffsetDateTime dateTime = parsedDate.atOffset(ZoneOffset.ofHours(-5));
                                LocalDateTime dateTimeForLocal = dateTime.atZoneSameInstant(clock.getZone()).toLocalDateTime();

                                long open = pippetesFromDouble(Double.parseDouble(parts[part++]));
                                long high =  pippetesFromDouble(Double.parseDouble(parts[part++]));
                                long low =   pippetesFromDouble(Double.parseDouble(parts[part++]));
                                long close = pippetesFromDouble(Double.parseDouble(parts[part]));

                                values.put(dateTimeForLocal, new OHLC(open, high, low, close));

                                return true;
                            }

                            @Override
                            public NavigableMap<LocalDateTime, OHLC> getResult() {
                                return values;
                            }
                        });

                        NavigableSet<LocalDate> availableDates = result.keySet().stream()
                                .map(LocalDateTime::toLocalDate)
                                .collect(Collectors.toCollection(TreeSet::new));

                        LOG.info("Loaded {} in {}", pairYear, timer);

                        return new CurrencyData(ONE_MINUTE, result, availableDates);
                    }
                }
            });

    private final LoadingCache<CurrencyPairYear, CurrencyData> fiveMinuteCache = timeFrameAggregateCache(minuteCache, FIVE_MINUTE);
    private final LoadingCache<CurrencyPairYear, CurrencyData> fifteenMinuteCache = timeFrameAggregateCache(fiveMinuteCache, FIFTEEN_MINUTE);
    private final LoadingCache<CurrencyPairYear, CurrencyData> thirtyMinuteCache = timeFrameAggregateCache(fifteenMinuteCache, THIRTY_MINUTE);
    private final LoadingCache<CurrencyPairYear, CurrencyData> oneHourCache = timeFrameAggregateCache(thirtyMinuteCache, ONE_HOUR);
    private final LoadingCache<CurrencyPairYear, CurrencyData> fourHourCache = timeFrameAggregateCache(oneHourCache, FOUR_HOURS);
    private final LoadingCache<CurrencyPairYear, CurrencyData> oneDayCache = timeFrameAggregateCache(fourHourCache, ONE_DAY);
    private final LoadingCache<CurrencyPairYear, CurrencyData> oneWeekCache = timeFrameAggregateCache(oneDayCache, ONE_WEEK);
    private final LoadingCache<CurrencyPairYear, CurrencyData> oneMonthCache = timeFrameAggregateCache(oneDayCache, ONE_MONTH);
    // TODO: Need to simulate an "ongoing" candle, which is what a live broker returns for the current minute, five minute, hour, day, etc.
    private final ImmutableMap<CandleTimeFrame, LoadingCache<CurrencyPairYear, CurrencyData>> caches = ImmutableMap.<CandleTimeFrame, LoadingCache<CurrencyPairYear, CurrencyData>>
            builder()
            .put(ONE_MINUTE, minuteCache)
            .put(FIVE_MINUTE, fiveMinuteCache)
            .put(FIFTEEN_MINUTE, fifteenMinuteCache)
            .put(THIRTY_MINUTE, thirtyMinuteCache)
            .put(ONE_HOUR, oneHourCache)
            .put(FOUR_HOURS, fourHourCache)
            .put(ONE_DAY, oneDayCache)
            .put(ONE_WEEK, oneWeekCache)
            .put(ONE_MONTH, oneMonthCache)
            .build();

    private final MarketTime clock;

    @Autowired
    HistoryDataCurrencyPairService(MarketTime clock) {
        this.clock = clock;
    }

    @Override
    public Optional<CurrencyPairHistory> getData(Instrument pair, LocalDateTime time) {
        boolean inverse = pair.isInverse();
        int year = time.getYear();
        Instrument lookupInstrument = pair.getBrokerInstrument();
        CurrencyData currencyData = minuteCache.getUnchecked(new CurrencyPairYear(lookupInstrument, year));
        Map<LocalDateTime, OHLC> yearData = currencyData.ohlcData;
        OHLC ohlc = yearData.get(time);
        if (inverse && ohlc != null) {
            ohlc = ohlc.inverse();
        }

        return ohlc == null ? Optional.empty() : Optional.of(new CurrencyPairHistory(pair, time, ohlc));
    }

    @Override
    public Set<LocalDate> getAvailableDays(Instrument pair, int year) {
        CurrencyPairYear key = new CurrencyPairYear(pair.getBrokerInstrument(), year);
        CurrencyData currencyData = minuteCache.getUnchecked(key);
        return currencyData.availableDates;
    }

    @Override
    public NavigableMap<LocalDateTime, OHLC> getOneDayCandles(Instrument pair, Range<LocalDateTime> closed) {
        return getOHLC(ONE_DAY, pair, closed);
    }

    @Override
    public NavigableMap<LocalDateTime, OHLC> getFourHourCandles(Instrument pair, Range<LocalDateTime> closed) {
        return getOHLC(FOUR_HOURS, pair, closed);
    }

    private NavigableMap<LocalDateTime, OHLC> getOHLC(CandleTimeFrame timeFrame, Instrument pair, Range<LocalDateTime> between) {
        NavigableMap<LocalDateTime, OHLC> result = new TreeMap<>();
        LocalDateTime start = timeFrame.calculateStart(between.lowerEndpoint());
        LocalDateTime end = timeFrame.calculateStart(between.upperEndpoint());

        int startYear = start.getYear();
        int endYear = end.getYear();

        boolean inverse = pair.isInverse();
        Instrument lookupInstrument = pair.getBrokerInstrument();

        for (int year = startYear; year <= endYear; year++) {
            result.putAll(caches.get(timeFrame).getUnchecked(new CurrencyPairYear(lookupInstrument, year)).ohlcData);
        }

        result = new TreeMap<>(result.subMap(start, end));

        if (inverse && !result.isEmpty()) {
            for (LocalDateTime time : new ArrayList<>(result.keySet())) {
                OHLC ohlc = result.get(time);
                result.put(time, ohlc.inverse());
            }
        }
        return result;
    }

    private static LoadingCache<CurrencyPairYear, CurrencyData> timeFrameAggregateCache(LoadingCache<CurrencyPairYear, CurrencyData> delegate, CandleTimeFrame timeFrame) {
        return CacheBuilder.newBuilder()
                .build(new CacheLoader<CurrencyPairYear, CurrencyData>() {
                    @Override
                    public CurrencyData load(CurrencyPairYear pairYear) throws Exception {
                        Stopwatch timer = Stopwatch.createStarted();

                        CurrencyData currencyData = delegate.get(pairYear);
                        NavigableMap<LocalDateTime, OHLC> result = timeFrame.aggregate(currencyData.ohlcData);

                        LOG.info("Loaded {} ({}) in {}", pairYear, timeFrame, timer);

                        return new CurrencyData(timeFrame, result, currencyData.availableDates);
                    }
                });
    }
}
