package forex.broker;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Objects;

import static forex.broker.Quote.invert;

public class CandlestickData {

    private final long o;
    private final long h;
    private final long l;
    private final long c;

    public CandlestickData(long o, long h, long l, long c) {
        this.o = o;
        this.h = h;
        this.l = l;
        this.c = c;
    }

    public long getO() {
        return o;
    }

    public long getH() {
        return h;
    }

    public long getL() {
        return l;
    }

    public long getC() {
        return c;
    }

    @Override
    public boolean equals(Object o1) {
        if (this == o1) return true;
        if (o1 == null || getClass() != o1.getClass()) return false;
        CandlestickData that = (CandlestickData) o1;
        return o == that.o &&
                h == that.h &&
                l == that.l &&
                c == that.c;
    }

    @Override
    public int hashCode() {
        return Objects.hash(o, h, l, c);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("o", o)
                .add("h", h)
                .add("l", l)
                .add("c", c)
                .toString();
    }

    public static CandlestickData aggregate(Collection<CandlestickData> values) {
        Preconditions.checkArgument(!values.isEmpty());

        long open = values.iterator().next().getO();
        long close = Iterables.getLast(values).getC();
        long high = Math.max(open, close);
        long low = Math.min(open, close);

        for (CandlestickData value : values) {
            high = Math.max(high, value.getH());
            low = Math.min(low, value.getL());
        }

        return new CandlestickData(open, high, low, close);
    }

    public static CandlestickData inverse(CandlestickData candlestickData) {
        return new CandlestickData(invert(candlestickData.getO()), invert(candlestickData.getL()),
                invert(candlestickData.getH()), invert(candlestickData.getC()));
    }
}
