package forex.market;

import java.time.LocalDateTime;

public interface OhlcProjection {

    LocalDateTime getOpen();

    long getHigh();

    long getLow();

    LocalDateTime getClose();
}
