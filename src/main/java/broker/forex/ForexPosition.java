package broker.forex;

import broker.Stance;
import com.google.common.base.MoreObjects;
import market.forex.Instrument;

import java.time.LocalDateTime;
import java.util.Objects;

public class ForexPosition {

    private final LocalDateTime opened;
    private final Instrument pair;
    private final Stance stance;
    private final double price;

    public ForexPosition(LocalDateTime opened, Instrument pair, Stance stance, double price) {
        this.opened = opened;
        this.pair = pair;
        this.stance = stance;
        this.price = price;
    }

    public LocalDateTime getOpened() {
        return opened;
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
        return Double.compare(that.price, price) == 0 &&
                Objects.equals(opened, that.opened) &&
                pair == that.pair &&
                stance == that.stance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(opened, pair, stance, price);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("opened", opened)
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
