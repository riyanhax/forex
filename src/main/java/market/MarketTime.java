package market;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static java.time.LocalDateTime.parse;
import static java.time.format.TextStyle.NARROW;
import static market.InstrumentHistoryService.DATE_TIME_FORMATTER;

public interface MarketTime {

    ZoneId ZONE = ZoneId.of("America/Chicago");
    String ZONE_NAME = ZONE.getDisplayName(NARROW, Locale.US);

    ZoneId ZONE_UTC = ZoneId.of("UTC");
    ZoneId ZONE_NEW_YORK = ZoneId.of("America/New_York");

    int END_OF_TRADING_DAY_HOUR = ZonedDateTime.of(LocalDateTime.of(2017, Month.JANUARY, 3, 17, 0), MarketTime.ZONE_NEW_YORK)
            .withZoneSameInstant(MarketTime.ZONE).getHour();

    DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    LocalDateTime now();

    LocalDate nowLocalDate();

    default LocalDate tomorrow() {
        return nowLocalDate().plusDays(1);
    }

    static String formatTimestamp(LocalDateTime time) {
        return TIMESTAMP_FORMATTER.format(time);
    }
    static String formatRange(LocalDateTime start, LocalDateTime end ) {
        return formatTimestamp(start) + " - " + formatTimestamp(end);
    }

    static String formatRange(String start, String end ) {
        return start + " - " + end;
    }

    default ZoneId getZone() {
        return ZONE;
    }

    static LocalDateTime parseTimestamp(String timestamp) {
        return timestamp == null ? null : parse(timestamp, DATE_TIME_FORMATTER.withZone(MarketTime.ZONE));
    }

}
