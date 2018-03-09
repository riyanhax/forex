package market;

import broker.CandlestickData;

import java.time.LocalDateTime;

public class InstrumentHistory {

    public final Instrument instrument;
    public final LocalDateTime timestamp;
    public final CandlestickData ohlc;

    public InstrumentHistory(Instrument instrument, LocalDateTime timestamp, CandlestickData ohlc) {
        this.instrument = instrument;
        this.timestamp = timestamp;
        this.ohlc = ohlc;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public CandlestickData getOHLC() {
        return ohlc;
    }
}
