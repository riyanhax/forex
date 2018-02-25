package market.forex;

import market.InstrumentHistoryImpl;
import market.OHLC;

import java.time.LocalDateTime;

class CurrencyPairHistory extends InstrumentHistoryImpl<CurrencyPair> {

    public CurrencyPairHistory(CurrencyPair instrument, LocalDateTime timestamp, OHLC ohlc) {
        super(instrument, timestamp, ohlc);
    }

}
