package broker;

import java.time.LocalDateTime;

class UnitsPositionValue implements PositionValue {

    private final Position position;
    private final LocalDateTime timestamp;
    private final double price;
    private final double units;

    UnitsPositionValue(Position position, LocalDateTime timestamp, double units, double price) {
        this.position = position;
        this.timestamp = timestamp;
        this.units = units;
        this.price = price;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public double getUnits() {
        return units;
    }

    @Override
    public LocalDateTime timestamp() {
        return timestamp;
    }

    @Override
    public double value() {
        return price * units;
    }
}
