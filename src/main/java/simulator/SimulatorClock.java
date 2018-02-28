package simulator;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class SimulatorClock extends Clock {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    public abstract LocalDateTime now();

    public abstract LocalDate nowLocalDate();

    public abstract LocalDate tomorrow();

    public static String formatTimestamp(LocalDateTime time) {
        return TIMESTAMP_FORMATTER.format(time);
    }
    public static String formatRange(LocalDateTime start, LocalDateTime end ) {
        return formatTimestamp(start) + " - " + formatTimestamp(end);
    }

}
