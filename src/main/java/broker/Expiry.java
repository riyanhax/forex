package broker;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public enum Expiry {
    // TODO: Oanda includes 1,2,3,4,5,6,12,18 hours + 1,2 days + 1 week + 1,2,3 months
    THREE_MONTHS(3, ChronoUnit.MONTHS);

    private final long modifier;
    private final TemporalUnit unit;

    Expiry(long modifier, TemporalUnit unit) {
        this.modifier = modifier;
        this.unit = unit;
    }

    public LocalDateTime getExpiration(LocalDateTime from) {
        return from.plus(modifier, unit);
    }
}
