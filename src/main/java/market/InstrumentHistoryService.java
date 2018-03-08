package market;

import com.google.common.collect.Range;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NavigableMap;

public interface InstrumentHistoryService {

    NavigableMap<LocalDateTime, OHLC> getOneDayCandles(Instrument pair, Range<LocalDateTime> closed) throws Exception;

    NavigableMap<LocalDateTime, OHLC> getFourHourCandles(Instrument pair, Range<LocalDateTime> closed) throws Exception;

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    default ZonedDateTime parseToZone(String time, ZoneId zone) {
        return ZonedDateTime.parse(time.substring(0, 19) + "Z", DATE_TIME_FORMATTER.withZone(zone));
    }
}
