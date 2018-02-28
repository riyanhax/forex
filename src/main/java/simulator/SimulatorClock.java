package simulator;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class SimulatorClock extends Clock {

    public abstract LocalDateTime now();

    public abstract LocalDate nowLocalDate();

    public abstract LocalDate tomorrow();
}
