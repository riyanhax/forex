package broker;

public class Price {
    private double closeoutBid;
    private double closeoutAsk;

    public Price(double closeoutBid, double closeoutAsk) {
        this.closeoutBid = closeoutBid;
        this.closeoutAsk = closeoutAsk;
    }

    public double getCloseoutBid() {
        return closeoutBid;
    }

    public double getCloseoutAsk() {
        return closeoutAsk;
    }
}
