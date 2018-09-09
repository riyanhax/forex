package simulator;

import market.MarketTime;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;

class SimulatorClock implements MarketTime {

    private Instant instant;
    private LocalDateTime now;
    private LocalDate today;
    private LocalDate tomorrow;

    SimulatorClock(SimulatorProperties simulatorProperties) {
        this(simulatorProperties.getStartTime());
    }

    SimulatorClock(LocalDateTime now) {
        init(now);
    }

    private void init(LocalDateTime startTime) {
        instant = startTime.atZone(ZONE).toInstant();
        configure();
    }

    void advance(long amount, TemporalUnit unit) {
        instant = instant.plus(amount, unit);
        configure();
    }

    private void configure() {
        Clock clock = Clock.fixed(instant, ZONE);
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
