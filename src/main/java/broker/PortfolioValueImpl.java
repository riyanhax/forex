package broker;

import java.time.LocalDateTime;
import java.util.Set;

public abstract class PortfolioValueImpl<I extends Instrument, P extends Position<I>, PV extends PositionValue<I>> implements PortfolioValue<I, P> {
    private final Portfolio<I, P> portfolio;
    private final LocalDateTime timestamp;
    private final Set<PV> positionValues;

    public PortfolioValueImpl(Portfolio<I, P> portfolio, LocalDateTime timestamp, Set<PV> positionValues) {
        this.portfolio = portfolio;
        this.timestamp = timestamp;
        this.positionValues = positionValues;
    }

    @Override
    public double getCash() {
        return portfolio.getCash();
    }

    @Override
    public Set<P> getPositions() {
        return portfolio.getPositions();
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public double marketValue() {
        double value = getCash();
        for (PV position : positionValues) {
            value += position.value();
        }

        return value;
    }
}
