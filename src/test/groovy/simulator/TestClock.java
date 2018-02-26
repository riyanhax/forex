package simulator;

import java.time.LocalDateTime;

public class TestClock extends SimulatorClockImpl {

    public TestClock(LocalDateTime now) {
        init(now);
    }

}
