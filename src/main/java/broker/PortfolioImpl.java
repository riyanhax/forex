package broker;

import java.util.Set;

public abstract class PortfolioImpl implements Portfolio {
    private final double cash;
    private final Set<Position> positions;

    public PortfolioImpl(double cash, Set<Position> positions) {
        this.cash = cash;
        this.positions = positions;
    }

    @Override
    public double getCash() {
        return cash;
    }

    @Override
    public Set<Position> getPositions() {
        return positions;
    }
}
