package broker;

import java.time.LocalDateTime;
import java.util.Set;

public abstract class PortfolioValueImpl implements PortfolioValue {

    private final Portfolio portfolio;
    private final LocalDateTime timestamp;
    private final Set<PositionValue> positionValues;

    public PortfolioValueImpl(Portfolio portfolio, LocalDateTime timestamp, Set<PositionValue> positionValues) {
        this.portfolio = portfolio;
        this.timestamp = timestamp;
        this.positionValues = positionValues;
    }

    @Override
    public double getCash() {
        return portfolio.getCash();
    }

    @Override
    public Set<Position> getPositions() {
        return portfolio.getPositions();
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public double marketValue() {
        double value = getCash();
        for (PositionValue position : positionValues) {
            value += position.value();
        }

        return value;
    }
}
