package broker.forex;

import broker.Position;
import com.google.common.base.MoreObjects;
import market.forex.Instrument;

import java.util.Objects;

public class ForexPosition implements Position {

    private final Instrument pair;
    private final int units;

    public ForexPosition(Instrument pair, int units) {
        this.pair = pair;
        this.units = units;
    }

    @Override
    public Instrument getInstrument() {
        return pair;
    }

    @Override
    public int getShares() {
        return units;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForexPosition that = (ForexPosition) o;
        return units == that.units &&
                Objects.equals(pair, that.pair);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pair, units);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("pair", pair)
                .add("units", units)
                .toString();
    }
}
