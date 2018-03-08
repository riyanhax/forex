package market;

import broker.Quote;
import broker.TradeSummary;
import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ForexPortfolioValue implements Comparable<ForexPortfolioValue> {

    private final ForexPortfolio portfolio;
    private final LocalDateTime timestamp;
    private final List<TradeSummary> positionValues;

    public ForexPortfolioValue(ForexPortfolio portfolio, LocalDateTime timestamp, List<TradeSummary> positionValues) {
        this.portfolio = portfolio;
        this.timestamp = timestamp;
        this.positionValues = positionValues;
    }

    public ForexPortfolio getPortfolio() {
        return portfolio;
    }

    public long getPipettesProfit() {
        return portfolio.getPipettesProfit();
    }

    public List<TradeSummary> getPositions() {
        return portfolio.getPositions();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public long pipettes() {
        return getPipettesProfit() + positionValues.stream()
                .map(TradeSummary::getUnrealizedPL)
                .mapToLong(Quote::pippetesFromDouble)
                .sum();
    }

    public List<TradeSummary> getPositionValues() {
        return positionValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForexPortfolioValue that = (ForexPortfolioValue) o;
        return Objects.equals(portfolio, that.portfolio) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(positionValues, that.positionValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(portfolio, timestamp, positionValues);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("portfolio", portfolio)
                .add("timestamp", timestamp)
                .add("positionValues", positionValues)
                .toString();
    }

    @Override
    public int compareTo(ForexPortfolioValue o) {
        return Comparator.comparing(ForexPortfolioValue::getTimestamp).compare(this, o);
    }
}
