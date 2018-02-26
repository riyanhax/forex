package market;

import market.forex.CurrencyPairHistory;
import market.forex.Instrument;

import java.time.LocalDateTime;
import java.util.Optional;

public interface InstrumentHistoryService {

    Optional<CurrencyPairHistory> getData(Instrument instrument, LocalDateTime time);

}
