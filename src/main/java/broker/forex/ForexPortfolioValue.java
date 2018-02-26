package broker.forex;

import broker.PortfolioValueImpl;
import broker.PositionValue;

import java.time.LocalDateTime;
import java.util.Set;

public class ForexPortfolioValue extends PortfolioValueImpl {

    public ForexPortfolioValue(ForexPortfolio portfolio, LocalDateTime timestamp, Set<PositionValue> positionValues) {
        super(portfolio, timestamp, positionValues);
    }

}
