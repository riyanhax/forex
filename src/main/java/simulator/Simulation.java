package simulator;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.of;
import static java.time.Month.DECEMBER;
import static java.time.Month.JUNE;

public class Simulation {

    public final LocalDateTime startTime;
    public final LocalDateTime endTime;
    public final long millisDelayBetweenMinutes;
    public final double pipSpread = 2;
    public final int instancesPerTraderType = 10;

    public Simulation() {
        this(of(2017, JUNE, 1, 1, 0), of(2017, DECEMBER, 31, 23, 59), 0L);
    }

    public Simulation(LocalDateTime startTime, LocalDateTime endTime, long realDelayPerMinute) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.millisDelayBetweenMinutes = realDelayPerMinute;
    }
}
