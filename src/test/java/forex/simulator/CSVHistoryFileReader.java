package forex.simulator;

import com.google.common.base.Stopwatch;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;
import forex.broker.CandlestickData;
import forex.market.Instrument;
import forex.market.OneMinuteCandleReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.NavigableMap;
import java.util.TreeMap;

import static forex.broker.Quote.pippetesFromDouble;
import static forex.market.MarketTime.ZONE;

public class CSVHistoryFileReader implements OneMinuteCandleReader {

    private static final Logger LOG = LoggerFactory.getLogger(CSVHistoryFileReader.class);
    private static final String HISTORY_FILE_PATTERN = "/history/DAT_ASCII_%s_M1_%d.csv";

    private final DateTimeFormatter timestampParser = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
    private final String historyFilePattern;

    public CSVHistoryFileReader() {
        this(HISTORY_FILE_PATTERN);
    }

    public CSVHistoryFileReader(String historyFilePattern) {
        this.historyFilePattern = historyFilePattern;
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> instrumentData(Instrument instrument, int year) throws IOException {
        Stopwatch timer = Stopwatch.createStarted();

        String path = String.format(historyFilePattern, instrument.name(), year);

        try (InputStreamReader is = new InputStreamReader(CSVHistoryFileReader.class.getResourceAsStream(path))) {

            NavigableMap<LocalDateTime, CandlestickData> result = CharStreams.readLines(is, new LineProcessor<NavigableMap<LocalDateTime, CandlestickData>>() {
                NavigableMap<LocalDateTime, CandlestickData> values = new TreeMap<>();

                @Override
                public boolean processLine(String line) {
                    int part = 0;
                    String[] parts = line.split(";");
                    LocalDateTime parsedDate = LocalDateTime.parse(parts[part++], timestampParser).withSecond(0);
                    // The files are at UTC-5 (no daylight savings timestamp observed)
                    OffsetDateTime dateTime = parsedDate.atOffset(ZoneOffset.ofHours(-5));
                    LocalDateTime dateTimeForLocal = dateTime.atZoneSameInstant(ZONE).toLocalDateTime();

                    double open = Double.parseDouble(parts[part++]);
                    double high = Double.parseDouble(parts[part++]);
                    double low = Double.parseDouble(parts[part++]);
                    double close = Double.parseDouble(parts[part]);

                    values.put(dateTimeForLocal, new CandlestickData(pippetesFromDouble(false, open), pippetesFromDouble(false, high),
                            pippetesFromDouble(false, low), pippetesFromDouble(false, close)));

                    return true;
                }

                @Override
                public NavigableMap<LocalDateTime, CandlestickData> getResult() {
                    return values;
                }
            });

            LOG.info("Loaded {}-{} in {}", instrument, year, timer);

            return result;
        } catch (Exception e) {
            LOG.error("Unable to load data!", e);
            return new TreeMap<>();
        }
    }


}
