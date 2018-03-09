package market;

import broker.CandlestickData;
import com.google.common.collect.Range;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NavigableMap;

public interface InstrumentHistoryService {

    NavigableMap<LocalDateTime, CandlestickData> getOneDayCandles(Instrument pair, Range<LocalDateTime> closed) throws Exception;

    NavigableMap<LocalDateTime, CandlestickData> getFourHourCandles(Instrument pair, Range<LocalDateTime> closed) throws Exception;

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;
}
