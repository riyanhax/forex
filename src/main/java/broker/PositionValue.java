package broker;

import java.time.LocalDateTime;

public interface PositionValue {

    Position getPosition();

    double getUnits();

    LocalDateTime timestamp();

    double value();

}
