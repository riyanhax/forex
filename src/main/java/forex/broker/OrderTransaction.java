package forex.broker;

import forex.market.Instrument;

public interface OrderTransaction {

    Instrument getInstrument();

    void setInstrument(Instrument instrument);

    int getUnits();

    void setUnits(int units);

}
