package broker;

import market.order.OrderRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public enum Expiry {
    // TODO: Oanda includes 1,2,3,4,5,6,12,18 hours + 1,2 days + 1 week + 1,2,3 months
    NONE(1000000, ChronoUnit.DAYS), THREE_MONTHS(3, ChronoUnit.MONTHS);

    private final long modifier;
    private final TemporalUnit unit;

    Expiry(long modifier, TemporalUnit unit) {
        this.modifier = modifier;
        this.unit = unit;
    }

    public boolean isAfter(LocalDateTime now, OrderRequest order) {
        return now.plus(modifier, unit).isAfter(order.getSubmissionDate());
    }
}
