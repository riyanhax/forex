package broker.forex;

import java.time.LocalDateTime;
import java.util.Set;

public class ForexPortfolioValue {

    private final ForexPortfolio portfolio;
    private final LocalDateTime timestamp;
    private final Set<ForexPositionValue> positionValues;

    public ForexPortfolioValue(ForexPortfolio portfolio, LocalDateTime timestamp, Set<ForexPositionValue> positionValues) {
        this.portfolio = portfolio;
        this.timestamp = timestamp;
        this.positionValues = positionValues;
    }

    public double getPipsProfit() {
        return portfolio.getPipsProfit();
    }

    public Set<ForexPosition> getPositions() {
        return portfolio.getPositions();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double pips() {
        return getPipsProfit() + positionValues.stream().mapToDouble(ForexPositionValue::pips).sum();
    }

    public Set<ForexPositionValue> getPositionValues() {
        return positionValues;
    }
}
