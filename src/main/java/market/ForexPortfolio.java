package market;

import com.google.common.base.MoreObjects;

import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

public class ForexPortfolio {

    private final long pipettesProfit;
    private final Set<ForexPosition> positions;
    private final SortedSet<ForexPositionValue> closedTrades;

    public ForexPortfolio(long pipettesProfit, Set<ForexPosition> positions, SortedSet<ForexPositionValue> closedTrades) {
        this.pipettesProfit = pipettesProfit;
        this.positions = positions;
        this.closedTrades = closedTrades;
    }

    public long getPipettesProfit() {
        return pipettesProfit;
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
        return Double.compare(that.pipettesProfit, pipettesProfit) == 0 &&
                Objects.equals(positions, that.positions) &&
                Objects.equals(closedTrades, that.closedTrades);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pipettesProfit, positions, closedTrades);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("pipettesProfit", pipettesProfit)
                .add("positions", positions)
                .add("closedTrades", closedTrades)
                .toString();
    }
}
