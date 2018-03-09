package broker;

public class Price {
    private long closeoutBid;
    private long closeoutAsk;

    public Price(long closeoutBid, long closeoutAsk) {
        this.closeoutBid = closeoutBid;
        this.closeoutAsk = closeoutAsk;
    }

    public long getCloseoutBid() {
        return closeoutBid;
    }

    public long getCloseoutAsk() {
        return closeoutAsk;
    }
}
