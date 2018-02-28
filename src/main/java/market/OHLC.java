package market;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import java.util.Collection;

public class OHLC {
    public final double open;
    public final double high;
    public final double low;
    public final double close;

    public OHLC(double open, double high, double low, double close) {
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public OHLC inverse() {
        return new OHLC(1 / open, 1 / high, 1 / close, 1 / low);
    }

    public static OHLC aggregate(Collection<OHLC> values) {
        Preconditions.checkArgument(!values.isEmpty());

        double open = values.iterator().next().open;
        double close = Iterables.getLast(values).close;
        double high = Math.max(open, close);
        double low = Math.min(open, close);

        for (OHLC value : values) {
            high = Math.max(high, value.high);
            low = Math.min(low, value.low);
        }

        return new OHLC(open, high, low, close);
    }
}
