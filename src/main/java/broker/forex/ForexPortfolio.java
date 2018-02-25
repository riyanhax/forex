package broker.forex;

import broker.PortfolioImpl;
import instrument.CurrencyPair;

import java.util.Set;

class ForexPortfolio extends PortfolioImpl<CurrencyPair, ForexPosition> {

    public ForexPortfolio(double cash, Set<ForexPosition> positions) {
        super(cash, positions);
    }

}
