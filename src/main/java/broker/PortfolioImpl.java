package broker;

import java.util.Set;
import java.util.stream.Collectors;

class PortfolioImpl implements Portfolio {
    private final Set<PositionValue> positionValues;

    public PortfolioImpl(Set<PositionValue> positionValues) {
        this.positionValues = positionValues;
    }

    @Override
    public double getCash() {
        return System.currentTimeMillis() % 100;
    }

    @Override
    public Set<Position> getPositions() {
        return positionValues.stream().map(PositionValue::getPosition).collect(Collectors.toSet());
    }

    @Override
    public double getValue() {
        return getCash() + getPositions().size() * 100;
    }
}
