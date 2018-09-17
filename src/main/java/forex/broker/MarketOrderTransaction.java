package forex.broker;

import com.google.common.base.MoreObjects;
import forex.market.Instrument;

import java.time.LocalDateTime;
import java.util.Objects;

public class MarketOrderTransaction {

    private final String id;
    private final LocalDateTime time;
    private final Instrument instrument;
    private final int units;

    public MarketOrderTransaction(String id, LocalDateTime time, Instrument instrument, int units) {
        this.id = id;
        this.time = time;
        this.instrument = instrument;
        this.units = units;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public int getUnits() {
        return units;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketOrderTransaction that = (MarketOrderTransaction) o;
        return units == that.units &&
                Objects.equals(id, that.id) &&
                Objects.equals(time, that.time) &&
                instrument == that.instrument;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, time, instrument, units);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("time", time)
                .add("instrument", instrument)
                .add("units", units)
                .toString();
    }
}
