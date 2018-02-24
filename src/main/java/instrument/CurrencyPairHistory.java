package instrument;

import java.time.LocalDateTime;

public class CurrencyPairHistory {

    public final CurrencyPair pair;
    public final LocalDateTime time;
    public final OHLC ohlc;

    public CurrencyPairHistory(CurrencyPair pair, LocalDateTime time, OHLC ohlc) {
        this.pair = pair;
        this.time = time;
        this.ohlc = ohlc;
    }
}
