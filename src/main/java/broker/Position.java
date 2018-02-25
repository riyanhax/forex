package broker;

import market.Instrument;

public interface Position<I extends Instrument> {

    I getInstrument();

    int getShares();
}
