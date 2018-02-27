package broker;

import java.time.LocalDateTime;
import java.util.Set;

public interface PortfolioValue extends Portfolio {

    LocalDateTime getTimestamp();

    double marketValue();

    Set<PositionValue> getPositionValues();

}
