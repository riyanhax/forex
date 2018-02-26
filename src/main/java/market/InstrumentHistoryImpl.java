package market;

import market.forex.Instrument;

import java.time.LocalDateTime;

public class InstrumentHistoryImpl implements InstrumentHistory {

    public final Instrument instrument;
    public final LocalDateTime timestamp;
    public final OHLC ohlc;

    public InstrumentHistoryImpl(Instrument instrument, LocalDateTime timestamp, OHLC ohlc) {
        this.instrument = instrument;
        this.timestamp = timestamp;
        this.ohlc = ohlc;
    }

    @Override
    public Instrument getInstrument() {
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
