package market;

import com.google.common.collect.Range;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;

public interface InstrumentHistoryService {

    NavigableMap<LocalDateTime, OHLC> getOHLC(CandleTimeFrame timeFrame, Instrument pair, Range<LocalDateTime> between);

    Optional<CurrencyPairHistory> getData(Instrument instrument, LocalDateTime time);

    Set<LocalDate> getAvailableDays(Instrument instrument, int year);
}
