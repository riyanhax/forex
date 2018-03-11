package broker;

import market.Instrument;

public class Price {
    private final Instrument instrument;
    private final long closeoutBid;
    private final long closeoutAsk;

    public Price(Instrument instrument, long closeoutBid, long closeoutAsk) {
        this.instrument = instrument;
        this.closeoutBid = closeoutBid;
        this.closeoutAsk = closeoutAsk;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public long getCloseoutBid() {
        return closeoutBid;
    }

    public long getCloseoutAsk() {
        return closeoutAsk;
    }
}
