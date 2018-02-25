package broker;

import market.Instrument;

import java.util.Set;

public interface Portfolio<I extends Instrument, P extends Position<I>> {

    double getCash();

    Set<P> getPositions();
}
