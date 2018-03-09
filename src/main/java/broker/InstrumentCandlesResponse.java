package broker;

import market.Instrument;

import java.util.List;

public class InstrumentCandlesResponse {
    private final Instrument instrument;
    private final CandlestickGranularity granularity;
    private final List<Candlestick> candles;

    public InstrumentCandlesResponse(Instrument instrument, CandlestickGranularity granularity, List<Candlestick> candles) {
        this.instrument = instrument;
        this.granularity = granularity;
        this.candles = candles;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public CandlestickGranularity getGranularity() {
        return granularity;
    }

    public List<Candlestick> getCandles() {
        return candles;
    }
}
