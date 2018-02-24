package simulator;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class SimulatorClock extends Clock {

    public LocalDateTime now() {
        return LocalDateTime.now(this);
    }

    public LocalDate nowLocalDate() {
        return now().toLocalDate();
    }
}
