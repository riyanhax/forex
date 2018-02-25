package broker;

import java.util.Set;

public abstract class PortfolioImpl<I extends Instrument, P extends Position<I>> implements Portfolio<I, P> {
    private final double cash;
    private final Set<P> positions;

    public PortfolioImpl(double cash, Set<P> positions) {
        this.cash = cash;
        this.positions = positions;
    }

    @Override
    public double getCash() {
        return cash;
    }

    @Override
    public Set<P> getPositions() {
        return positions;
    }
}
