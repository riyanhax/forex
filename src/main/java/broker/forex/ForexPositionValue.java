package broker.forex;

import broker.Stance;
import com.google.common.base.MoreObjects;
import market.forex.Instrument;

import java.util.Objects;

public class ForexPositionValue {

    private final ForexPosition position;
    private final double currentPrice;

    ForexPositionValue(ForexPosition position, double currentPrice) {
        this.position = position;
        this.currentPrice = currentPrice;
    }

    public ForexPosition getPosition() {
        return position;
    }

    public double pips() {
        return position.pipsProfit(currentPrice);
    }

    public Instrument getInstrument() {
        return position.getInstrument();
    }

    public Stance getStance() {
        return position.getStance();
    }

    public double getPrice() {
        return position.getPrice();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForexPositionValue that = (ForexPositionValue) o;
        return Double.compare(that.currentPrice, currentPrice) == 0 &&
                Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, currentPrice);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("position", position)
                .add("currentPrice", currentPrice)
                .toString();
    }
}
