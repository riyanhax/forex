package simulator;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalUnit;

@Service
class SimulatorClockImpl extends SimulatorClock {

    private static final ZoneId ZONE = ZoneId.of("America/Chicago");

    private Clock clock;

    void init(LocalDateTime startTime) {
        Instant instant = startTime.atZone(ZONE).toInstant();
        this.clock = Clock.fixed(instant, ZONE);
    }

    void advance(long amount, TemporalUnit unit) {
        this.clock = Clock.fixed(instant().plus(amount, unit), clock.getZone());
    }

    @Override
    public ZoneId getZone() {
        return clock.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return clock.withZone(zone);
    }

    @Override
    public Instant instant() {
        return clock.instant();
    }
}
