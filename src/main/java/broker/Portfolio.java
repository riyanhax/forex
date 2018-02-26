package broker;

import java.util.Set;

public interface Portfolio {

    double getCash();

    Set<Position> getPositions();
}
