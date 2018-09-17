package forex.market;

import java.time.LocalDate;

public interface Market {

    long getPrice(Instrument instrument);

    boolean isAvailable();

    boolean isAvailable(LocalDate date);

    void processUpdates();
}
