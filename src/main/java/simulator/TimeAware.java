package simulator;

import java.time.LocalDateTime;

public interface TimeAware {

    void advanceTime(LocalDateTime previous, LocalDateTime now);

}
