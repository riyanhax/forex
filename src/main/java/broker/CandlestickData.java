package broker;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import java.util.Collection;

public class CandlestickData {

    private final double o;
    private final double h;
    private final double l;
    private final double c;

    public CandlestickData(double o, double h, double l, double c) {
        this.o = o;
        this.h = h;
        this.l = l;
        this.c = c;
    }

    public double getO() {
        return o;
    }

    public double getH() {
        return h;
    }

    public double getL() {
        return l;
    }

    public double getC() {
        return c;
    }

    public static CandlestickData aggregate(Collection<CandlestickData> values) {
        Preconditions.checkArgument(!values.isEmpty());

        double open = values.iterator().next().getO();
        double close = Iterables.getLast(values).getC();
        double high = Math.max(open, close);
        double low = Math.min(open, close);

        for (CandlestickData value : values) {
            high = Math.max(high, value.getH());
            low = Math.min(low, value.getL());
        }

        return new CandlestickData(open, high, low, close);
    }

    public static CandlestickData inverse(CandlestickData candlestickData) {
        return new CandlestickData(1d / candlestickData.getO(), 1d / candlestickData.getL(),
                1d / candlestickData.getH(), 1d / candlestickData.getC());
    }
}
