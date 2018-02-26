package market.order;

import market.forex.Instrument;

public interface Order {

    Instrument getInstrument();

    int getUnits();
}
