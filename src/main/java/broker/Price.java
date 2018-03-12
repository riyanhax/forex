package broker;

import com.google.common.base.MoreObjects;
import market.Instrument;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price = (Price) o;
        return closeoutBid == price.closeoutBid &&
                closeoutAsk == price.closeoutAsk &&
                instrument == price.instrument;
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrument, closeoutBid, closeoutAsk);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("instrument", instrument)
                .add("closeoutBid", closeoutBid)
                .add("closeoutAsk", closeoutAsk)
                .toString();
    }
}
