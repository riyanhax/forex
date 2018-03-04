package market;

import java.time.LocalDateTime;

public interface Watcher {
    void run();

    boolean keepGoing(LocalDateTime now);

    long millisUntilNextInterval();

    boolean logTime(LocalDateTime now);
}
