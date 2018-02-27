package broker.forex;

import broker.Position;
import broker.PositionValue;
import com.google.common.base.MoreObjects;
import market.forex.Instrument;

import java.util.Objects;

public class ForexPositionValue implements PositionValue {

    private final Position position;
    private final double price;

    ForexPositionValue(Position position, double price) {
        this.position = position;
        this.price = price;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public double value() {
        return price * getShares();
    }

    @Override
    public Instrument getInstrument() {
        return position.getInstrument();
    }

    @Override
    public int getShares() {
        return position.getShares();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForexPositionValue that = (ForexPositionValue) o;
        return Double.compare(that.price, price) == 0 &&
                Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, price);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("position", position)
                .add("price", price)
                .toString();
    }
}
