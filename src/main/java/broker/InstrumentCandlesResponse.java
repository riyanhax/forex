package broker;

import java.util.List;

public class InstrumentCandlesResponse {
    private final String instrument;
    private final CandlestickGranularity granularity;
    private final List<Candlestick> candles;

    public InstrumentCandlesResponse(String instrument, CandlestickGranularity granularity, List<Candlestick> candles) {
        this.instrument = instrument;
        this.granularity = granularity;
        this.candles = candles;
    }

    public String getInstrument() {
        return instrument;
    }

    public CandlestickGranularity getGranularity() {
        return granularity;
    }

    public List<Candlestick> getCandles() {
        return candles;
    }
}
