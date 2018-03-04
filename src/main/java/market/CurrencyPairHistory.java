package market;

import java.time.LocalDateTime;

public class CurrencyPairHistory extends InstrumentHistoryImpl {

    public CurrencyPairHistory(Instrument instrument, LocalDateTime timestamp, OHLC ohlc) {
        super(instrument, timestamp, ohlc);
    }

}
