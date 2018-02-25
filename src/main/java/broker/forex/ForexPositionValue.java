package broker.forex;

import broker.PositionValue;
import market.forex.CurrencyPair;

import java.util.Objects;

class ForexPositionValue implements PositionValue<CurrencyPair, ForexPosition> {

    private final ForexPosition position;
    private final double price;

    ForexPositionValue(ForexPosition position, double price) {
        this.position = position;
        this.price = price;
    }

    @Override
    public ForexPosition getPosition() {
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
