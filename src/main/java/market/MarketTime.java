package market;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public interface MarketTime {

    ZoneId ZONE = ZoneId.of("America/Chicago");

    DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    LocalDateTime now();

    LocalDate nowLocalDate();

    LocalDate tomorrow();

    static String formatTimestamp(LocalDateTime time) {
        return TIMESTAMP_FORMATTER.format(time);
    }
    static String formatRange(LocalDateTime start, LocalDateTime end ) {
        return formatTimestamp(start) + " - " + formatTimestamp(end);
    }

    ZoneId getZone();
}
