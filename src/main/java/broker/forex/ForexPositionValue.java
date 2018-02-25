package broker.forex;

import broker.Position;
import broker.PositionValue;
import instrument.CurrencyPair;

import java.util.Objects;

class ForexPositionValue implements PositionValue<CurrencyPair> {

    private final Position<CurrencyPair> position;
    private final double price;

    ForexPositionValue(Position<CurrencyPair> position, double price) {
        this.position = position;
        this.price = price;
    }

    @Override
    public Position<CurrencyPair> getPosition() {
        return position;
    }

    @Override
    public double value() {
        return price * getShares();
    }

    @Override
    public CurrencyPair getInstrument() {
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
}
