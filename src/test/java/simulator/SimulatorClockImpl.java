package simulator;

import market.MarketTime;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;

class SimulatorClockImpl implements MarketTime {

    private Instant instant;
    private LocalDateTime now;
    private LocalDate today;
    private LocalDate tomorrow;
    private Clock clock;

    public SimulatorClockImpl(Simulation simulation) {
        this(simulation.getStartTime());
    }

    SimulatorClockImpl(LocalDateTime now) {
        init(now);
    }

    void init(LocalDateTime startTime) {
        instant = startTime.atZone(ZONE).toInstant();
        configure();
    }

    void advance(long amount, TemporalUnit unit) {
        instant = instant.plus(amount, unit);
        configure();
    }

    private void configure() {
        clock = Clock.fixed(instant, ZONE);
        now = LocalDateTime.now(clock);
        today = now.toLocalDate();
        tomorrow = today.plusDays(1);
    }

    @Override
    public LocalDateTime now() {
        return now;
    }

    @Override
    public LocalDate nowLocalDate() {
        return today;
    }

    @Override
    public LocalDate tomorrow() {
        return tomorrow;
    }
}
