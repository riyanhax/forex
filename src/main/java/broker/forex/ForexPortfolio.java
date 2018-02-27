package broker.forex;

import broker.PortfolioImpl;
import broker.Position;

import java.util.Set;

public class ForexPortfolio extends PortfolioImpl {

    public ForexPortfolio(double cash, Set<Position> positions) {
        super(cash, positions);
    }

}
