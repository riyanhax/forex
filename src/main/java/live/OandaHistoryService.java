package live;

import com.google.common.collect.Range;
import market.CandleTimeFrame;
import market.OHLC;
import market.forex.CurrencyPairHistory;
import market.forex.CurrencyPairHistoryService;
import market.forex.Instrument;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;

@Service
class OandaHistoryService implements CurrencyPairHistoryService {
    @Override
    public NavigableMap<LocalDateTime, OHLC> getOHLC(CandleTimeFrame timeFrame, Instrument pair, Range<LocalDateTime> between) {
        return null;
    }

    @Override
    public Optional<CurrencyPairHistory> getData(Instrument instrument, LocalDateTime time) {
        return Optional.empty();
    }

    @Override
    public Set<LocalDate> getAvailableDays(Instrument instrument, int year) {
        return null;
    }
}
