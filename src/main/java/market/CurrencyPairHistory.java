package market;

import broker.CandlestickData;

import java.time.LocalDateTime;

public class CurrencyPairHistory extends InstrumentHistoryImpl {

    public CurrencyPairHistory(Instrument instrument, LocalDateTime timestamp, CandlestickData ohlc) {
        super(instrument, timestamp, ohlc);
    }

}
