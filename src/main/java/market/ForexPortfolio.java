package market;

import com.google.common.base.MoreObjects;

import java.util.Objects;
import java.util.Set;

public class ForexPortfolio {

    private final long pipettesProfit;
    private final Set<ForexPosition> positions;

    public ForexPortfolio(long pipettesProfit, Set<ForexPosition> positions) {
        this.pipettesProfit = pipettesProfit;
        this.positions = positions;
    }

    public long getPipettesProfit() {
        return pipettesProfit;
    }

    public Set<ForexPosition> getPositions() {
        return positions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForexPortfolio that = (ForexPortfolio) o;
        return Double.compare(that.pipettesProfit, pipettesProfit) == 0 &&
                Objects.equals(positions, that.positions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pipettesProfit, positions);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("pipettesProfit", pipettesProfit)
                .add("positions", positions)
                .toString();
    }
}
