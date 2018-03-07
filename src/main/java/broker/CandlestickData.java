package broker;

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
}
