package market;

import broker.CandlestickData;

import java.time.LocalDateTime;

public class InstrumentHistoryImpl implements InstrumentHistory {

    public final Instrument instrument;
    public final LocalDateTime timestamp;
    public final CandlestickData ohlc;

    public InstrumentHistoryImpl(Instrument instrument, LocalDateTime timestamp, CandlestickData ohlc) {
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
    public CandlestickData getOHLC() {
        return ohlc;
    }
}
