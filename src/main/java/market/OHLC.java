package market;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import java.util.Collection;

import static broker.Quote.invert;

public class OHLC {
    public final long open;
    public final long high;
    public final long low;
    public final long close;

    public OHLC(long open, long high, long low, long close) {
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public OHLC inverse() {
        return new OHLC(invert(open), invert(low), invert(close), invert(high));
    }

    public static OHLC aggregate(Collection<OHLC> values) {
        Preconditions.checkArgument(!values.isEmpty());

        long open = values.iterator().next().open;
        long close = Iterables.getLast(values).close;
        long high = Math.max(open, close);
        long low = Math.min(open, close);

        for (OHLC value : values) {
            high = Math.max(high, value.high);
            low = Math.min(low, value.low);
        }

        return new OHLC(open, high, low, close);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("open", open)
                .add("high", high)
                .add("low", low)
                .add("close", close)
                .toString();
    }
}
