package broker;

import java.util.Set;

public interface Portfolio<I extends Instrument, P extends Position<I>> {

    double getCash();

    Set<P> getPositions();
}
