package broker;

import market.Instrument;

import java.time.LocalDateTime;

public interface PortfolioValue<INSTRUMENT extends Instrument, POSITION extends Position<INSTRUMENT>>
        extends Portfolio<INSTRUMENT, POSITION> {

    LocalDateTime getTimestamp();

    double marketValue();

}
