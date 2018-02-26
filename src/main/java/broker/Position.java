package broker;

import market.forex.Instrument;

public interface Position {

    Instrument getInstrument();

    int getShares();
}
