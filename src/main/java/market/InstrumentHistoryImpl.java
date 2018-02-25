package market;

import java.time.LocalDateTime;

public class InstrumentHistoryImpl<I extends Instrument> implements InstrumentHistory<I> {

    public final I instrument;
    public final LocalDateTime timestamp;
    public final OHLC ohlc;

    public InstrumentHistoryImpl(I instrument, LocalDateTime timestamp, OHLC ohlc) {
        this.instrument = instrument;
        this.timestamp = timestamp;
        this.ohlc = ohlc;
    }

    @Override
    public I getInstrument() {
        return instrument;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public OHLC getOHLC() {
        return ohlc;
    }
}
