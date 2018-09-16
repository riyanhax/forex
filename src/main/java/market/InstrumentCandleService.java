package market;

import broker.RequestException;
import com.google.common.collect.Range;

import java.time.LocalDateTime;
import java.util.List;

public interface InstrumentCandleService {

    List<InstrumentCandle> retrieveAndStoreOneMinuteCandles(Range<LocalDateTime> inclusiveRange) throws RequestException;

    LocalDateTime findLatestStoredMinute();
}
