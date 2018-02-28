package market;

import market.forex.CurrencyPairHistory;
import market.forex.Instrument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

public interface InstrumentHistoryService {

    Optional<CurrencyPairHistory> getData(Instrument instrument, LocalDateTime time);

    Set<LocalDate> getAvailableDays(Instrument instrument, int year);
}
