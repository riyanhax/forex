package market;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.DayOfWeek.FRIDAY;

public interface MarketTime {

    ZoneId ZONE = ZoneId.of("America/Chicago");
    ZoneId ZONE_NEW_YORK = ZoneId.of("America/New_York");

    int END_OF_TRADING_DAY_HOUR = ZonedDateTime.of(LocalDateTime.of(2017, Month.JANUARY, 3, 17, 0), MarketTime.ZONE_NEW_YORK)
            .withZoneSameInstant(MarketTime.ZONE).getHour();

    DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    DayOfWeek WEEKLY_ALIGNMENT = FRIDAY;

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
}
