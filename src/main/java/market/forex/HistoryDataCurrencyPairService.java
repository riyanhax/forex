package market.forex;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

@Service
class HistoryDataCurrencyPairService implements CurrencyPairHistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryDataCurrencyPairService.class);

    private static class CurrencyPairYear {
        final CurrencyPair pair;
        final int year;

        private CurrencyPairYear(CurrencyPair pair, int year) {
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

    private final DateTimeFormatter timestampParser = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
    private final LoadingCache<CurrencyPairYear, NavigableMap<LocalDateTime, OHLC>> cache = CacheBuilder.newBuilder()
            .build(new CacheLoader<CurrencyPairYear, NavigableMap<LocalDateTime, OHLC>>() {
                @Override
                public NavigableMap<LocalDateTime, OHLC> load(CurrencyPairYear pairYear) throws Exception {

                    Stopwatch timer = Stopwatch.createStarted();

                    String path = String.format("/history/DAT_ASCII_%s_M1_%d.csv", pairYear.pair.getSymbol(), pairYear.year);
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

                                double open = Double.parseDouble(parts[part++]);
                                double high = Double.parseDouble(parts[part++]);
                                double low = Double.parseDouble(parts[part++]);
                                double close = Double.parseDouble(parts[part]);

                                values.put(dateTimeForLocal, new OHLC(open, high, low, close));

                                return true;
                            }

                            @Override
                            public NavigableMap<LocalDateTime, OHLC> getResult() {
                                return values;
                            }
                        });

                        LOG.info("Loaded {} in {}", pairYear, timer);

                        return result;
                    }
                }
            });

    private final SimulatorClock clock;

    @Autowired
    HistoryDataCurrencyPairService(SimulatorClock clock) {
        this.clock = clock;
    }

    @Override
    public Optional<CurrencyPairHistory> getData(CurrencyPair pair, LocalDateTime time) {
        OHLC ohlc = null;
        int year = time.getYear();
        try {
            NavigableMap<LocalDateTime, OHLC> yearData = cache.get(new CurrencyPairYear(pair, year));
            ohlc = yearData.get(time);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return ohlc == null ? Optional.empty() : Optional.of(new CurrencyPairHistory(pair, time, ohlc));
    }
}
