package broker.forex;

import broker.PortfolioValueImpl;
import market.forex.CurrencyPair;

import java.time.LocalDateTime;
import java.util.Set;

public class ForexPortfolioValue extends PortfolioValueImpl<CurrencyPair, ForexPosition, ForexPositionValue> {

    public ForexPortfolioValue(ForexPortfolio portfolio, LocalDateTime timestamp, Set<ForexPositionValue> positionValues) {
        super(portfolio, timestamp, positionValues);
    }

}
