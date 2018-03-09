package broker;

import com.google.common.base.MoreObjects;
import market.Instrument;

import java.time.LocalDateTime;
import java.util.Comparator;

public class TradeSummary implements Comparable<TradeSummary> {
    private final Instrument instrument;
    private final int currentUnits;
    private final long price;
    private final long realizedProfitLoss;
    private final long unrealizedProfitLoss;
    private final LocalDateTime openTime;
    private final LocalDateTime closeTime;
    private final String id;

    public TradeSummary(Instrument instrument, int currentUnits, long price, long realizedProfitLoss,
                        long unrealizedProfitLoss, LocalDateTime openTime, LocalDateTime closeTime, String id) {
        this.instrument = instrument;
        this.currentUnits = currentUnits;
        this.price = price;
        this.realizedProfitLoss = realizedProfitLoss;
        this.unrealizedProfitLoss = unrealizedProfitLoss;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.id = id;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public int getCurrentUnits() {
        return this.currentUnits;
    }

    public long getPrice() {
        return price;
    }

    public long getRealizedProfitLoss() {
        return realizedProfitLoss;
    }

    public long getUnrealizedPL() {
        return unrealizedProfitLoss;
    }

    public LocalDateTime getOpenTime() {
        return openTime;
    }

    public LocalDateTime getCloseTime() {
        return closeTime;
    }

    public String getId() {
        return id;
    }

    @Override
    public int compareTo(TradeSummary o) {
        return Comparator.comparing(TradeSummary::getOpenTime).compare(this, o);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("instrument", instrument)
                .add("currentUnits", currentUnits)
                .add("price", price)
                .add("realizedProfitLoss", realizedProfitLoss)
                .add("unrealizedProfitLoss", unrealizedProfitLoss)
                .add("openTime", openTime)
                .add("closeTime", closeTime)
                .add("id", id)
                .toString();
    }
}
