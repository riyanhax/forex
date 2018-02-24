package instrument;

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
}
