package simulator;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalUnit;

@Service
class SimulatorClockImpl extends SimulatorClock {

    private static final ZoneId ZONE = ZoneId.of("America/Chicago");
    private Instant instant;
    private LocalDateTime now;
    private LocalDate today;
    private LocalDate tomorrow;

    void init(LocalDateTime startTime) {
        instant = startTime.atZone(ZONE).toInstant();
        configure();
    }

    void advance(long amount, TemporalUnit unit) {
        instant = instant.plus(amount, unit);
        configure();
    }

    private void configure() {
        now = LocalDateTime.now(this);
        today = now.toLocalDate();
        tomorrow = today.plusDays(1);
    }

    @Override
    public ZoneId getZone() {
        return ZONE;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this.withZone(zone);
    }

    @Override
    public Instant instant() {
        return instant;
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
