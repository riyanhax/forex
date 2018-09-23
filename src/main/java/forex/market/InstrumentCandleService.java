package forex.market;

import com.google.common.collect.Range;
import forex.broker.RequestException;

import java.time.LocalDateTime;
import java.util.List;

public interface InstrumentCandleService {

    List<InstrumentCandle> retrieveAndStoreOneMinuteCandles(Range<LocalDateTime> inclusiveRange) throws RequestException;

    LocalDateTime findLatestStoredMinute();
}
