package broker;

import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;
import java.util.Objects;

public class Candlestick {
    private final LocalDateTime time;
    private final CandlestickData bid;
    private final CandlestickData ask;
    private final CandlestickData mid;

    public Candlestick(LocalDateTime time, CandlestickData bid, CandlestickData ask, CandlestickData mid) {
        this.time = time;
        this.bid = bid;
        this.ask = ask;
        this.mid = mid;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public CandlestickData getBid() {
        return bid;
    }

    public CandlestickData getAsk() {
        return ask;
    }

    public CandlestickData getMid() {
        return mid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Candlestick that = (Candlestick) o;
        return Objects.equals(time, that.time) &&
                Objects.equals(bid, that.bid) &&
                Objects.equals(ask, that.ask) &&
                Objects.equals(mid, that.mid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, bid, ask, mid);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("time", time)
                .add("bid", bid)
                .add("ask", ask)
                .add("mid", mid)
                .toString();
    }
}
