package market;

import java.time.LocalDateTime;

public interface InstrumentHistory<I extends Instrument> {

    I getInstrument();

    LocalDateTime getTimestamp();

    OHLC getOHLC();

}
