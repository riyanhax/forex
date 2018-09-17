package forex.broker;

import com.google.common.base.MoreObjects;
import forex.market.Instrument;

import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstrumentCandlesResponse that = (InstrumentCandlesResponse) o;
        return instrument == that.instrument &&
                granularity == that.granularity &&
                Objects.equals(candles, that.candles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrument, granularity, candles);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("instrument", instrument)
                .add("granularity", granularity)
                .add("candles", candles)
                .toString();
    }
}
