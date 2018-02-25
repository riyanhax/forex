package broker;

import market.Instrument;

public interface PositionValue<INSTRUMENT extends Instrument, POSITION extends Position<INSTRUMENT>> extends Position<INSTRUMENT> {

    POSITION getPosition();

    double value();
}
