package broker;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import market.Instrument;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static broker.Quote.doubleFromPippetes;
import static broker.Quote.profitLossDisplay;
import static com.google.common.base.Preconditions.checkState;
import static market.MarketTime.formatTimestamp;

public class TradeSummary {
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

    public long getUnrealizedProfitLoss() {
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

    public long getCurrentPrice() {
        return  price + ((closeTime == null ? unrealizedProfitLoss : realizedProfitLoss) / currentUnits);
    }

    public long getPurchaseValue() {
        return price * currentUnits;
    }

    public long getNetAssetValue() {
        return getCurrentPrice() * getCurrentUnits();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeSummary that = (TradeSummary) o;
        return currentUnits == that.currentUnits &&
                price == that.price &&
                realizedProfitLoss == that.realizedProfitLoss &&
                unrealizedProfitLoss == that.unrealizedProfitLoss &&
                instrument == that.instrument &&
                Objects.equals(openTime, that.openTime) &&
                Objects.equals(closeTime, that.closeTime) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrument, currentUnits, price, realizedProfitLoss, unrealizedProfitLoss, openTime, closeTime, id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("instrument", instrument)
                .add("currentUnits", currentUnits)
                .add("price", doubleFromPippetes(price))
                .add("currentPrice", doubleFromPippetes(getCurrentPrice()))
                .add("realizedProfitLoss", profitLossDisplay(realizedProfitLoss))
                .add("unrealizedProfitLoss", profitLossDisplay(unrealizedProfitLoss))
                .add("openTime", formatTimestamp(openTime))
                .add("closeTime", closeTime == null ? null : formatTimestamp(closeTime))
                .add("id", id)
                .toString();
    }

    private TradeSummary stateChanged(CalculatedTradeState calculatedTradeState) {
        checkState(id.equals(calculatedTradeState.getId()));

        long newUnrealizedProfitLoss = calculatedTradeState.getUnrealizedProfitLoss();

        return new TradeSummary(instrument, currentUnits, price, realizedProfitLoss,
                newUnrealizedProfitLoss, openTime, closeTime, id);
    }

    static List<TradeSummary> incorporateState(List<TradeSummary> trades, AccountChangesState stateChanges) {
        List<CalculatedTradeState> tradeStates = stateChanges.getTrades();
        if (tradeStates.isEmpty()) {
            return trades;
        }

        ImmutableMap<String, CalculatedTradeState> stateByTradeId = Maps.uniqueIndex(tradeStates, CalculatedTradeState::getId);
        ImmutableMap<String, TradeSummary> tradesById = Maps.uniqueIndex(trades, TradeSummary::getId);

        checkState(stateByTradeId.size() == tradesById.size(), "Trade and state count were different!");

        List<TradeSummary> updated = new ArrayList<>(trades.size());
        tradesById.forEach((id, trade) -> updated.add(trade.stateChanged(stateByTradeId.get(id))));

        return updated;
    }
}
