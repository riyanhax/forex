package market.forex;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;
import market.OHLC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import simulator.SimulatorClock;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
        final ImmutableMap<LocalDateTime, OHLC> ohlcData;
        final ImmutableSet<LocalDate> availableDates;

        public CurrencyData(Map<LocalDateTime, OHLC> ohlcData, Set<LocalDate> availableDates) {
            this.ohlcData = ImmutableMap.copyOf(ohlcData);
            this.availableDates = ImmutableSet.copyOf(availableDates);
        }
    }

    private final DateTimeFormatter timestampParser = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
    private final LoadingCache<CurrencyPairYear, CurrencyData> cache = CacheBuilder.newBuilder()
            .build(new CacheLoader<CurrencyPairYear, CurrencyData>() {
                @Override
                public CurrencyData load(CurrencyPairYear pairYear) throws Exception {

                    Stopwatch timer = Stopwatch.createStarted();

                    String path = String.format("/history/DAT_ASCII_%s_M1_%d.csv", pairYear.pair.getSymbol(), pairYear.year);
                    try (InputStreamReader is = new InputStreamReader(HistoryDataCurrencyPairService.class.getResourceAsStream(path))) {

                        Map<LocalDateTime, OHLC> result = CharStreams.readLines(is, new LineProcessor<Map<LocalDateTime, OHLC>>() {
                            Map<LocalDateTime, OHLC> values = new HashMap<>();

                            @Override
                            public boolean processLine(String line) throws IOException {
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

                                values.put(dateTimeForLocal, new OHLC(open, high, low, close));

                                return true;
                            }

                            @Override
                            public Map<LocalDateTime, OHLC> getResult() {
                                return values;
                            }
                        });

                        Set<LocalDate> availableDates = result.keySet().stream()
                                .map(LocalDateTime::toLocalDate)
                                .collect(Collectors.toCollection(HashSet::new));

                        LOG.info("Loaded {} in {}", pairYear, timer);

                        return new CurrencyData(result, availableDates);
                    }
                }
            });

    private final SimulatorClock clock;

    @Autowired
    HistoryDataCurrencyPairService(SimulatorClock clock) {
        this.clock = clock;
    }

    @Override
    public Optional<CurrencyPairHistory> getData(Instrument pair, LocalDateTime time) {
        boolean inverse = pair.isInverse();
        OHLC ohlc = null;
        int year = time.getYear();
        Instrument lookupInstrument = inverse ? pair.getOpposite() : pair;
        try {
            CurrencyData currencyData = cache.get(new CurrencyPairYear(lookupInstrument, year));
            Map<LocalDateTime, OHLC> yearData = currencyData.ohlcData;
            ohlc = yearData.get(time);
            if (inverse && ohlc != null) {
                ohlc = ohlc.inverse();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return ohlc == null ? Optional.empty() : Optional.of(new CurrencyPairHistory(pair, time, ohlc));
    }

    @Override
    public Set<LocalDate> getAvailableDays(Instrument pair, int year) {
        boolean inverse = pair.isInverse();
        Instrument lookupInstrument = inverse ? pair.getOpposite() : pair;
        try {
            CurrencyData currencyData = cache.get(new CurrencyPairYear(lookupInstrument, year));
            return currencyData.availableDates;
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return Collections.emptySet();
    }
}
