package market.order;

import market.forex.Instrument;

import java.util.Optional;

public interface Order {

    Instrument getInstrument();

    int getUnits();

    default boolean isSellOrder() {
        return false;
    }

    default boolean isBuyOrder() {
        return false;
    }

    default boolean isLimit() {
        return limit().isPresent();
    }

    Optional<Double> limit();
}
