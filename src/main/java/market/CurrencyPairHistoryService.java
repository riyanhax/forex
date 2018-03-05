package market;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

public interface CurrencyPairHistoryService extends InstrumentHistoryService {

    Optional<CurrencyPairHistory> getData(Instrument instrument, LocalDateTime time);

    Set<LocalDate> getAvailableDays(Instrument instrument, int year);
}
