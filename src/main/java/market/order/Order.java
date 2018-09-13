package market.order;

import market.Instrument;

import java.time.LocalDateTime;
import java.util.Optional;

public interface Order {

    Instrument getInstrument();

    int getUnits();

    Optional<LocalDateTime> expiration();

    Optional<Long> limit();

    default boolean isSellOrder() {
        return false;
    }

    default boolean isBuyOrder() {
        return false;
    }
}
