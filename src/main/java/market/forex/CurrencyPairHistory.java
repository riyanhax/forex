package market.forex;

import market.InstrumentHistoryImpl;
import market.OHLC;

import java.time.LocalDateTime;

public class CurrencyPairHistory extends InstrumentHistoryImpl {

    public CurrencyPairHistory(Instrument instrument, LocalDateTime timestamp, OHLC ohlc) {
        super(instrument, timestamp, ohlc);
    }

}
