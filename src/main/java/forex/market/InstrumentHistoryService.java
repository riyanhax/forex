package forex.market;

import com.google.common.collect.Range;
import forex.broker.CandlestickData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;

public interface InstrumentHistoryService {

    NavigableMap<LocalDateTime, CandlestickData> getOneDayCandles(Instrument pair, Range<LocalDateTime> closed);

    NavigableMap<LocalDateTime, CandlestickData> getFourHourCandles(Instrument pair, Range<LocalDateTime> closed);

    NavigableMap<LocalDateTime, CandlestickData> getFiveMinuteCandles(Instrument instrument, Range<LocalDateTime> closed);

    NavigableMap<LocalDateTime, CandlestickData> getOneMinuteCandles(Instrument instrument, Range<LocalDateTime> closed);

    NavigableMap<LocalDateTime, CandlestickData> getOneWeekCandles(Instrument pair, Range<LocalDateTime> range);

    Optional<InstrumentHistory> getData(Instrument instrument, LocalDateTime time);

    Set<LocalDate> getAvailableDays(Instrument instrument, int year);
}
