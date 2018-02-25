package instrument;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import simulator.SimulatorClock;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
class HistoryDataCurrencyPairService implements CurrencyPairService {

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
    }

    private final DateTimeFormatter timestampParser = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
    private final LoadingCache<CurrencyPairYear, Map<LocalDateTime, OHLC>> cache = CacheBuilder.newBuilder()
            .build(new CacheLoader<CurrencyPairYear, Map<LocalDateTime, OHLC>>() {
                @Override
                public Map<LocalDateTime, OHLC> load(CurrencyPairYear pairYear) throws Exception {

                    String path = String.format("/history/DAT_ASCII_%s_M1_%d.csv", pairYear.pair.getSymbol(), pairYear.year);
                    try (InputStreamReader is = new InputStreamReader(HistoryDataCurrencyPairService.class.getResourceAsStream(path))) {

                        return CharStreams.readLines(is, new LineProcessor<Map<LocalDateTime, OHLC>>() {
                            Map<LocalDateTime, OHLC> values = new HashMap<>();

                            @Override
                            public boolean processLine(String line) throws IOException {
                                int part = 0;
                                String[] parts = line.split(";");
                                LocalDateTime parsedDate = LocalDateTime.parse(parts[part++], timestampParser);
                                // The files are at UTC-5 (no daylight savings time observed)
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
                    }
                }
            });

    private final SimulatorClock clock;

    @Autowired
    public HistoryDataCurrencyPairService(SimulatorClock clock) {
        this.clock = clock;
    }

    @Override
    public CurrencyPairHistory getData(CurrencyPair pair, LocalDateTime time) {
        int year = time.getYear();
        try {
            Map<LocalDateTime, OHLC> yearData = cache.get(new CurrencyPairYear(pair, year));

            for (LocalDateTime timestamp = time; timestamp.getYear() == year; timestamp = timestamp.minusMinutes(1)) {
                OHLC ohlc = yearData.get(timestamp);
                if (ohlc != null) {
                    return new CurrencyPairHistory(pair, timestamp, ohlc);
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
