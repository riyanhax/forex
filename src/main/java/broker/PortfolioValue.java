package broker;

import java.time.LocalDateTime;

public interface PortfolioValue<I extends Instrument, P extends Position<I>> extends Portfolio<I, P> {

    LocalDateTime getTimestamp();

    double marketValue();

}
