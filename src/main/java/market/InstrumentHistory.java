package market;

import broker.CandlestickData;

import java.time.LocalDateTime;

public interface InstrumentHistory {

    Instrument getInstrument();

    LocalDateTime getTimestamp();

    CandlestickData getOHLC();

}
