package broker;

import com.google.common.base.MoreObjects;

public class BidAsk implements Quote {

    private final long bid;
    private final long ask;

    public BidAsk(long bid, long ask) {
        this.bid = bid;
        this.ask = ask;
    }

    @Override
    public long getBid() {
        return bid;
    }

    @Override
    public long getAsk() {
        return ask;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("bid", bid)
                .add("ask", ask)
                .toString();
    }
}
