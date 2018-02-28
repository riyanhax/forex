package broker.forex;

import java.util.Set;

public class ForexPortfolio {

    private final double pipsProfit;
    private final Set<ForexPosition> positions;

    public ForexPortfolio(double pipsProfit, Set<ForexPosition> positions) {
        this.pipsProfit = pipsProfit;
        this.positions = positions;
    }

    public double getPipsProfit() {
        return pipsProfit;
    }

    public Set<ForexPosition> getPositions() {
        return positions;
    }

}
