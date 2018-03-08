package market;

import broker.TradeSummary;
import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.Objects;

public class ForexPortfolio {

    private final long pipettesProfit;
    private final List<TradeSummary> positions;

    public ForexPortfolio(long pipettesProfit, List<TradeSummary> positions) {
        this.pipettesProfit = pipettesProfit;
        this.positions = positions;
    }

    public long getPipettesProfit() {
        return pipettesProfit;
    }

    public List<TradeSummary> getPositions() {
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
