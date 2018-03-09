package broker;

import java.time.LocalDateTime;

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
}
