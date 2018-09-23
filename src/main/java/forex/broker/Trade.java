package forex.broker;

import com.google.common.base.MoreObjects;
import forex.market.Instrument;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static forex.broker.Quote.doubleFromPippetes;
import static forex.broker.Quote.profitLossDisplay;
import static forex.market.MarketTime.formatTimestamp;

public class Trade {

    private final String id;
    private final Instrument instrument;
    private final long price;
    private final LocalDateTime openTime;
    private final TradeState state;
    private final int initialUnits;
    private final int currentUnits;
    private final long realizedProfitLoss;
    private final long unrealizedProfitLoss;
    private final long marginUsed;
    private final long averageClosePrice;
    private final List<String> closingTransactionIDs;
    private final long financing;
    private final LocalDateTime closeTime;

    //    com.oanda.v20.trade.Trade setClientExtensions clientExtensions;
//TakeProfitOrder takeProfitOrder
//StopLossOrder stopLossOrder
    //TrailingStopLossOrder trailingStopLossOrder

    public Trade(String id, Instrument instrument, long price, LocalDateTime openTime, TradeState state, int initialUnits,
                 int currentUnits, long realizedProfitLoss, long unrealizedProfitLoss, long marginUsed, long averageClosePrice,
                 List<String> closingTransactionIDs, long financing, @Nullable LocalDateTime closeTime) {
        this.id = id;
        this.instrument = instrument;
        this.price = price;
        this.openTime = openTime;
        this.state = state;
        this.initialUnits = initialUnits;
        this.currentUnits = currentUnits;
        this.realizedProfitLoss = realizedProfitLoss;
        this.unrealizedProfitLoss = unrealizedProfitLoss;
        this.marginUsed = marginUsed;
        this.averageClosePrice = averageClosePrice;
        this.closingTransactionIDs = closingTransactionIDs;
        this.financing = financing;
        this.closeTime = closeTime;
    }

    public String getId() {
        return id;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public long getPrice() {
        return price;
    }

    public long getCurrentPrice() {
        return price + ((closeTime == null ? unrealizedProfitLoss : realizedProfitLoss) / currentUnits);
    }

    public LocalDateTime getOpenTime() {
        return openTime;
    }

    public TradeState getState() {
        return state;
    }

    public int getInitialUnits() {
        return initialUnits;
    }

    public int getCurrentUnits() {
        return currentUnits;
    }

    public long getRealizedProfitLoss() {
        return realizedProfitLoss;
    }

    public long getUnrealizedProfitLoss() {
        return unrealizedProfitLoss;
    }

    public long getMarginUsed() {
        return marginUsed;
    }

    public long getAverageClosePrice() {
        return averageClosePrice;
    }

    public List<String> getClosingTransactionIDs() {
        return closingTransactionIDs;
    }

    public long getFinancing() {
        return financing;
    }

    public LocalDateTime getCloseTime() {
        return closeTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return price == trade.price &&
                initialUnits == trade.initialUnits &&
                currentUnits == trade.currentUnits &&
                realizedProfitLoss == trade.realizedProfitLoss &&
                unrealizedProfitLoss == trade.unrealizedProfitLoss &&
                marginUsed == trade.marginUsed &&
                averageClosePrice == trade.averageClosePrice &&
                financing == trade.financing &&
                Objects.equals(id, trade.id) &&
                instrument == trade.instrument &&
                Objects.equals(openTime, trade.openTime) &&
                state == trade.state &&
                Objects.equals(closingTransactionIDs, trade.closingTransactionIDs) &&
                Objects.equals(closeTime, trade.closeTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, instrument, price, openTime, state, initialUnits, currentUnits, realizedProfitLoss, unrealizedProfitLoss, marginUsed, averageClosePrice, closingTransactionIDs, financing, closeTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("instrument", instrument)
                .add("price", doubleFromPippetes(price))
                .add("currentPrice", doubleFromPippetes(getCurrentPrice()))
                .add("openTime", formatTimestamp(openTime))
                .add("state", state)
                .add("initialUnits", initialUnits)
                .add("currentUnits", currentUnits)
                .add("realizedProfitLoss", profitLossDisplay(realizedProfitLoss))
                .add("unrealizedProfitLoss", profitLossDisplay(unrealizedProfitLoss))
                .add("marginUsed", marginUsed)
                .add("averageClosePrice", averageClosePrice)
                .add("closingTransactionIDs", closingTransactionIDs)
                .add("financing", financing)
                .add("closeTime", closeTime == null ? null : formatTimestamp(closeTime))
                .toString();
    }

}
