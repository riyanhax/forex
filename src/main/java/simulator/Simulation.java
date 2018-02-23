package simulator;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.of;
import static java.time.Month.JANUARY;

public class Simulation {
    public final LocalDateTime startTime;
    public final LocalDateTime endTime;
    public final long millisDelayBetweenMinutes;

    public Simulation() {
        this(of(2017, JANUARY, 1, 0, 0), of(2018, JANUARY, 1, 0, 0), 0L);
    }

    public Simulation(LocalDateTime startTime, LocalDateTime endTime, long realDelayPerMinute) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.millisDelayBetweenMinutes = realDelayPerMinute;
    }
}
