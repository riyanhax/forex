package market;

import java.time.LocalDateTime;

public interface InstrumentHistory {

    Instrument getInstrument();

    LocalDateTime getTimestamp();

    OHLC getOHLC();

}
