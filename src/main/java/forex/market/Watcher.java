package forex.market;

import java.time.LocalDateTime;

public interface Watcher {
    void run() throws Exception;

    boolean keepGoing(LocalDateTime now);

    long millisUntilNextInterval();

    boolean logTime(LocalDateTime now);
}
