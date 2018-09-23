package forex.market;

import com.google.common.base.MoreObjects;
import forex.broker.CandlestickGranularity;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class InstrumentCandleType implements Serializable {

    private Instrument instrument;
    private CandlestickGranularity granularity;
    private LocalDateTime time;

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public CandlestickGranularity getGranularity() {
        return granularity;
    }

    public void setGranularity(CandlestickGranularity granularity) {
        this.granularity = granularity;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstrumentCandleType that = (InstrumentCandleType) o;
        return instrument == that.instrument &&
                granularity == that.granularity &&
                Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrument, granularity, time);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("instrument", instrument)
                .add("granularity", granularity)
                .add("time", time)
                .toString();
    }
}
