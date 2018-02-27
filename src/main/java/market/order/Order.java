package market.order;

import market.forex.Instrument;

public interface Order {

    Instrument getInstrument();

    int getUnits();

    default boolean isSellOrder() {
        return false;
    }

    default boolean isBuyOrder() {
        return false;
    }
}
