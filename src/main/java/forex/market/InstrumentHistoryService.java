package forex.market;

import forex.broker.CandlestickData;
import com.google.common.collect.Range;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;

public interface InstrumentHistoryService {

    NavigableMap<LocalDateTime, CandlestickData> getOneDayCandles(Instrument pair, Range<LocalDateTime> closed) throws Exception;

    NavigableMap<LocalDateTime, CandlestickData> getFourHourCandles(Instrument pair, Range<LocalDateTime> closed) throws Exception;

    NavigableMap<LocalDateTime, CandlestickData> getFiveMinuteCandles(Instrument instrument, Range<LocalDateTime> closed);

    NavigableMap<LocalDateTime, CandlestickData> getOneMinuteCandles(Instrument instrument, Range<LocalDateTime> closed);

    NavigableMap<LocalDateTime, CandlestickData> getOneWeekCandles(Instrument pair, Range<LocalDateTime> range);

    Optional<InstrumentHistory> getData(Instrument instrument, LocalDateTime time);

    Set<LocalDate> getAvailableDays(Instrument instrument, int year);
}