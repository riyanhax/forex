package broker;

import market.Instrument;

import java.time.LocalDateTime;
import java.util.Set;

public abstract class PortfolioValueImpl<INSTRUMENT extends Instrument, POSITION extends Position<INSTRUMENT>,
        POSITION_VALUE extends PositionValue<INSTRUMENT, POSITION>> implements PortfolioValue<INSTRUMENT, POSITION> {

    private final Portfolio<INSTRUMENT, POSITION> portfolio;
    private final LocalDateTime timestamp;
    private final Set<POSITION_VALUE> positionValues;

    public PortfolioValueImpl(Portfolio<INSTRUMENT, POSITION> portfolio, LocalDateTime timestamp, Set<POSITION_VALUE> positionValues) {
        this.portfolio = portfolio;
        this.timestamp = timestamp;
        this.positionValues = positionValues;
    }

    @Override
    public double getCash() {
        return portfolio.getCash();
    }

    @Override
    public Set<POSITION> getPositions() {
        return portfolio.getPositions();
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public double marketValue() {
        double value = getCash();
        for (POSITION_VALUE position : positionValues) {
            value += position.value();
        }

        return value;
    }
}
