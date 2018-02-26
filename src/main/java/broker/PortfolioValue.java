package broker;

import java.time.LocalDateTime;

public interface PortfolioValue extends Portfolio {

    LocalDateTime getTimestamp();

    double marketValue();

}
