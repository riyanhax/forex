package instrument;

import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
class HistoryDataCurrencyPairService implements CurrencyPairService {

    private static final Map<ZonedDateTime, CurrencyPairHistory> history;

    static {
        history = new HashMap<>();
    }

    @Override
    public CurrencyPairHistory getData(CurrencyPair pair, ZonedDateTime time) {
        return new CurrencyPairHistory(pair, time, 1.025d, 1.035d, 1.021d, 1.022d);
    }
}
