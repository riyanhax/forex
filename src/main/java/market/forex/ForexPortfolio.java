package market.forex;

import com.google.common.base.MoreObjects;

import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

public class ForexPortfolio {

    private final double pipsProfit;
    private final Set<ForexPosition> positions;
    private final SortedSet<ForexPositionValue> closedTrades;

    public ForexPortfolio(double pipsProfit, Set<ForexPosition> positions, SortedSet<ForexPositionValue> closedTrades) {
        this.pipsProfit = pipsProfit;
        this.positions = positions;
        this.closedTrades = closedTrades;
    }

    public double getPipsProfit() {
        return pipsProfit;
    }

    public Set<ForexPosition> getPositions() {
        return positions;
    }

    public SortedSet<ForexPositionValue> getClosedTrades() {
        return closedTrades;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForexPortfolio that = (ForexPortfolio) o;
        return Double.compare(that.pipsProfit, pipsProfit) == 0 &&
                Objects.equals(positions, that.positions) &&
                Objects.equals(closedTrades, that.closedTrades);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pipsProfit, positions, closedTrades);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("pipsProfit", pipsProfit)
                .add("positions", positions)
                .add("closedTrades", closedTrades)
                .toString();
    }
}
