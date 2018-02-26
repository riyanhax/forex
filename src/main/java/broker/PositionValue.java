package broker;

public interface PositionValue extends Position {

    Position getPosition();

    double value();
}
