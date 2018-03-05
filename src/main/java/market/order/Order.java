package market.order;

import broker.Expiry;
import market.Instrument;

import java.util.Optional;

public interface Order {

    Instrument getInstrument();

    int getUnits();

    Optional<Expiry> expiry();

    Optional<Long> limit();

    Optional<Double> stop();

    default boolean isSellOrder() {
        return false;
    }
    default boolean isBuyOrder() {
        return false;
    }
}
