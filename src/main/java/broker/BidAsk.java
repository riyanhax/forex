package broker;

public class BidAsk implements Quote {

    private final double bid;
    private final double ask;

    public BidAsk(double bid, double ask) {
        this.bid = bid;
        this.ask = ask;
    }

    @Override
    public double getBid() {
        return bid;
    }

    @Override
    public double getAsk() {
        return ask;
    }
}
