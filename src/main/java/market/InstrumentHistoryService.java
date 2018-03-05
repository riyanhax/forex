package market;

import com.google.common.collect.Range;

import java.time.LocalDateTime;
import java.util.NavigableMap;

public interface InstrumentHistoryService {

    NavigableMap<LocalDateTime, OHLC> getOneDayCandles(Instrument pair, Range<LocalDateTime> closed);

    NavigableMap<LocalDateTime, OHLC> getFourHourCandles(Instrument pair, Range<LocalDateTime> closed);
}
