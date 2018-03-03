package market.forex;

import broker.Stance;
import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

public class ForexPositionValue implements Comparable<ForexPositionValue> {

    private final ForexPosition position;
    private final LocalDateTime timestamp;
    private final double currentPrice;

    public ForexPositionValue(ForexPosition position, LocalDateTime timestamp, double currentPrice) {
        this.position = position;
        this.timestamp = timestamp;
        this.currentPrice = currentPrice;
    }

    public ForexPosition getPosition() {
        return position;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
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
                Objects.equals(position, that.position) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, timestamp, currentPrice);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("position", position)
                .add("timestamp", timestamp)
                .add("currentPrice", currentPrice)
                .add("pips", pips())
                .toString();
    }

    @Override
    public int compareTo(ForexPositionValue o) {
        return Comparator.comparing(ForexPositionValue::getTimestamp).compare(this, o);
    }
}