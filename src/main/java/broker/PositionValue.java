package broker;

public interface PositionValue<I extends Instrument> extends Position<I> {

    Position<I> getPosition();

    double value();
}
