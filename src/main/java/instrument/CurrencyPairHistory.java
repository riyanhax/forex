package instrument;

import java.time.ZonedDateTime;

public class CurrencyPairHistory {

    public final CurrencyPair pair;
    public final ZonedDateTime time;
    public final double open;
    public final double high;
    public final double low;
    public final double close;

    public CurrencyPairHistory(CurrencyPair pair, ZonedDateTime time, double open, double high, double low, double close) {
        this.pair = pair;
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }
}
