package broker.forex;

import broker.Stance;
import com.google.common.base.MoreObjects;
import market.forex.Instrument;

import java.util.Objects;

public class ForexPosition {

    private final Instrument pair;
    private final Stance stance;
    private final double price;

    public ForexPosition(Instrument pair, Stance stance, double price) {
        this.pair = pair;
        this.stance = stance;
        this.price = price;
    }

    public Instrument getInstrument() {
        return pair;
    }

    public Stance getStance() {
        return stance;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForexPosition that = (ForexPosition) o;
        return stance == that.stance &&
                Double.compare(that.price, price) == 0 &&
                pair == that.pair;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pair, stance, price);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("pair", pair)
                .add("stance", stance)
                .add("price", price)
                .toString();
    }

    public double pipsProfit(double currentPrice) {
        double difference = getStance() == Stance.LONG ? currentPrice - getPrice() : getPrice() - currentPrice;
        return difference * (1 / getInstrument().getPip());
    }
}
