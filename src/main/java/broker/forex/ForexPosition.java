package broker.forex;

import broker.Position;
import market.forex.CurrencyPair;

import java.util.Objects;

public class ForexPosition implements Position<CurrencyPair> {

    private final CurrencyPair pair;
    private final int units;

    public ForexPosition(CurrencyPair pair, int units) {
        this.pair = pair;
        this.units = units;
    }

    @Override
    public CurrencyPair getInstrument() {
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
}
