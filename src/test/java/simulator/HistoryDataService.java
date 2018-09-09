package simulator;

import broker.CandlestickData;
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
import market.Instrument;
import market.InstrumentHistory;
import market.InstrumentHistoryService;
import market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
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
class HistoryDataService implements InstrumentHistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryDataService.class);
    private static final String HISTORY_FILE_PATTERN = "/history/DAT_ASCII_%s_M1_%d.csv";

    private static class CandleRequest {
        final CandleTimeFrame timeFrame;
        final Instrument pair;
        final Range<LocalDateTime> between;

        private CandleRequest(CandleTimeFrame timeFrame, Instrument pair, Range<LocalDateTime> between) {
            this.timeFrame = timeFrame;
            this.pair = pair;
            this.between = between;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CandleRequest that = (CandleRequest) o;
            return timeFrame == that.timeFrame &&
                    pair == that.pair &&
                    Objects.equals(between, that.between);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timeFrame, pair, between);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("timeFrame", timeFrame)
                    .add("pair", pair)
                    .add("between", between)
                    .toString();
        }
    }

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
        final NavigableMap<LocalDateTime, CandlestickData> ohlcData;
        final NavigableSet<LocalDate> availableDates;

        public CurrencyData(CandleTimeFrame timeFrame, NavigableMap<LocalDateTime, CandlestickData> ohlcData, NavigableSet<LocalDate> availableDates) {
            this.timeFrame = timeFrame;
            this.ohlcData = ohlcData;
            this.availableDates = availableDates;
        }
    }

    private final String historyFilePattern;
    private final DateTimeFormatter timestampParser = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
    private final LoadingCache<CurrencyPairYear, CurrencyData> minuteCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<CurrencyPairYear, CurrencyData>() {
                @Override
                public CurrencyData load(CurrencyPairYear pairYear) throws Exception {

                    Stopwatch timer = Stopwatch.createStarted();

                    Instrument pair = pairYear.pair;
                    boolean inverse = pair.isInverse();

                    String path = String.format(historyFilePattern, pair.getBrokerInstrument().name(), pairYear.year);
                    try (InputStreamReader is = new InputStreamReader(HistoryDataService.class.getResourceAsStream(path))) {

                        NavigableMap<LocalDateTime, CandlestickData> result = CharStreams.readLines(is, new LineProcessor<NavigableMap<LocalDateTime, CandlestickData>>() {
                            NavigableMap<LocalDateTime, CandlestickData> values = new TreeMap<>();

                            @Override
                            public boolean processLine(String line) {
                                int part = 0;
                                String[] parts = line.split(";");
                                LocalDateTime parsedDate = LocalDateTime.parse(parts[part++], timestampParser).withSecond(0);
                                // The files are at UTC-5 (no daylight savings timestamp observed)
                                OffsetDateTime dateTime = parsedDate.atOffset(ZoneOffset.ofHours(-5));
                                LocalDateTime dateTimeForLocal = dateTime.atZoneSameInstant(clock.getZone()).toLocalDateTime();

                                double open = Double.parseDouble(parts[part++]);
                                double high = Double.parseDouble(parts[part++]);
                                double low = Double.parseDouble(parts[part++]);
                                double close = Double.parseDouble(parts[part]);

                                if (inverse) {
                                    double actualHigh = low;
                                    low = high;
                                    high = actualHigh;
                                }

                                values.put(dateTimeForLocal, new CandlestickData(pippetesFromDouble(inverse, open), pippetesFromDouble(inverse, high),
                                        pippetesFromDouble(inverse, low), pippetesFromDouble(inverse, close)));

                                return true;
                            }

                            @Override
                            public NavigableMap<LocalDateTime, CandlestickData> getResult() {
                                return values;
                            }
                        });

                        NavigableSet<LocalDate> availableDates = result.keySet().stream()
                                .map(LocalDateTime::toLocalDate)
                                .collect(Collectors.toCollection(TreeSet::new));

                        LOG.info("Loaded {} in {}", pairYear, timer);

                        return new CurrencyData(ONE_MINUTE, result, availableDates);
                    } catch (Exception e) {
                        LOG.error("Unable to load data!", e);
                        return new CurrencyData(ONE_MINUTE, new TreeMap<>(), new TreeSet<>());
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
    HistoryDataService(MarketTime clock) {
        this(clock, HISTORY_FILE_PATTERN);
    }

    HistoryDataService(MarketTime clock, String historyFilePattern) {
        this.clock = clock;
        this.historyFilePattern = historyFilePattern;
    }

    @Override
    public Optional<InstrumentHistory> getData(Instrument pair, LocalDateTime time) {
        int year = time.getYear();
        CurrencyData currencyData = minuteCache.getUnchecked(new CurrencyPairYear(pair, year));
        Map<LocalDateTime, CandlestickData> yearData = currencyData.ohlcData;
        CandlestickData ohlc = yearData.get(time);

        return ohlc == null ? Optional.empty() : Optional.of(new InstrumentHistory(pair, time, ohlc));
    }

    @Override
    public Set<LocalDate> getAvailableDays(Instrument pair, int year) {
        CurrencyPairYear key = new CurrencyPairYear(pair.getBrokerInstrument(), year);
        CurrencyData currencyData = minuteCache.getUnchecked(key);
        return currencyData.availableDates;
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getOneDayCandles(Instrument pair, Range<LocalDateTime> closed) {
        return getOHLC(ONE_DAY, pair, closed);
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getFourHourCandles(Instrument pair, Range<LocalDateTime> closed) {
        return getOHLC(FOUR_HOURS, pair, closed);
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getFiveMinuteCandles(Instrument instrument, Range<LocalDateTime> closed) {
        return getOHLC(FIVE_MINUTE, instrument, closed);
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getOneMinuteCandles(Instrument instrument, Range<LocalDateTime> closed) {
        return loadCandleData(new CandleRequest(CandleTimeFrame.ONE_MINUTE, instrument, closed)); // No need to cache one minute data
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getOneWeekCandles(Instrument pair, Range<LocalDateTime> range) {
        return getOHLC(ONE_WEEK, pair, range);
    }

    NavigableMap<LocalDateTime, CandlestickData> getOHLC(CandleTimeFrame timeFrame, Instrument pair, Range<LocalDateTime> between) {
        return candleRequestCache.getUnchecked(new CandleRequest(timeFrame, pair, between));
    }

    private NavigableMap<LocalDateTime, CandlestickData> loadCandleData(CandleRequest candleRequest) {

        Range<LocalDateTime> between = candleRequest.between;
        CandleTimeFrame timeFrame = candleRequest.timeFrame;
        Instrument pair = candleRequest.pair;

        LocalDateTime requestedEnd = between.upperEndpoint();

        NavigableMap<LocalDateTime, CandlestickData> result = new TreeMap<>();
        LocalDateTime start = timeFrame.calculateStart(between.lowerEndpoint());
        LocalDateTime end = timeFrame.calculateStart(requestedEnd);

        int startYear = start.getYear();
        int endYear = end.getYear();

        for (int year = startYear; year <= endYear; year++) {
            result.putAll(caches.get(timeFrame).getUnchecked(new CurrencyPairYear(pair, year)).ohlcData);
        }

        // This uses an inclusive end, because that's how Oanda does it
        result = new TreeMap<>(result.subMap(start, true, end, true));

        // We have to create a pseudo-candle for the last one
        if (end.isBefore(requestedEnd)) {
            NavigableMap<LocalDateTime, CandlestickData> candlesToAggregate = new TreeMap<>();

            LocalDateTime candleStart = end;
            SortedSet<CandleTimeFrame> smallerTimeFrames = CandleTimeFrame.descendingSmallerThan(timeFrame);
            for (CandleTimeFrame candleType : smallerTimeFrames) {
                LocalDateTime nextCandle = candleType.nextCandle(candleStart);

                while (!nextCandle.isAfter(requestedEnd)) {
                    Range<LocalDateTime> range = Range.closed(candleStart, nextCandle);
                    candlesToAggregate.putAll(getOHLC(candleType, pair, range));
                    candleStart = nextCandle;

                    nextCandle = candleType.nextCandle(candleStart);
                }

                if (candleStart.isAfter(requestedEnd)) {
                    break;
                }
            }

            CandlestickData aggregate = CandlestickData.aggregate(candlesToAggregate.values());
            result.put(end, aggregate);
        }

        return result;
    }

    private LoadingCache<CandleRequest, NavigableMap<LocalDateTime, CandlestickData>> candleRequestCache = CacheBuilder.newBuilder()
            .maximumSize(300)
            .build(new CacheLoader<CandleRequest, NavigableMap<LocalDateTime, CandlestickData>>() {
                @Override
                public NavigableMap<LocalDateTime, CandlestickData> load(@Nonnull CandleRequest request) throws Exception {
                    return loadCandleData(request);
                }
            });

    private static LoadingCache<CurrencyPairYear, CurrencyData> timeFrameAggregateCache(LoadingCache<CurrencyPairYear, CurrencyData> delegate, CandleTimeFrame timeFrame) {
        return CacheBuilder.newBuilder()
                .build(new CacheLoader<CurrencyPairYear, CurrencyData>() {
                    @Override
                    public CurrencyData load(@Nonnull CurrencyPairYear pairYear) throws Exception {
                        Stopwatch timer = Stopwatch.createStarted();

                        CurrencyData currencyData = delegate.get(pairYear);
                        NavigableMap<LocalDateTime, CandlestickData> result = timeFrame.aggregate(currencyData.ohlcData);

                        LOG.info("Loaded {} ({}) in {}", pairYear, timeFrame, timer);

                        return new CurrencyData(timeFrame, result, currencyData.availableDates);
                    }
                });
    }
}
