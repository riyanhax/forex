package live;

import com.google.common.collect.Range;
import market.CurrencyPairHistory;
import market.CurrencyPairHistoryService;
import market.Instrument;
import market.InstrumentHistoryService;
import market.OHLC;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;

@Service
class OandaHistoryService implements InstrumentHistoryService {

    @Override
    public NavigableMap<LocalDateTime, OHLC> getOneDayCandles(Instrument pair, Range<LocalDateTime> closed) {
        return null;
    }

    @Override
    public NavigableMap<LocalDateTime, OHLC> getFourHourCandles(Instrument pair, Range<LocalDateTime> closed) {
        return null;
    }
}
