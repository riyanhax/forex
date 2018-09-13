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
    private final String id;
    private final Instrument instrument;
    private final long price;
    private final LocalDateTime openTime;
    private final int initialUnits;
    private final int currentUnits;
    private final long realizedProfitLoss;
    private final long unrealizedProfitLoss;
    private final LocalDateTime closeTime;

    public TradeSummary(String id, Instrument instrument, long price, LocalDateTime openTime, int initialUnits, int currentUnits, long realizedProfitLoss, long unrealizedProfitLoss, LocalDateTime closeTime) {
        this.id = id;
        this.instrument = instrument;
        this.price = price;
        this.openTime = openTime;
        this.initialUnits = initialUnits;
        this.currentUnits = currentUnits;
        this.realizedProfitLoss = realizedProfitLoss;
        this.unrealizedProfitLoss = unrealizedProfitLoss;
        this.closeTime = closeTime;
    }

    /**
     * Create a summary from a detailed {@link Trade}.
     */
    public TradeSummary(Trade trade) {
        this(trade.getId(), trade.getInstrument(), trade.getPrice(), trade.getOpenTime(), trade.getInitialUnits(),
                trade.getCurrentUnits(), trade.getRealizedProfitLoss(), trade.getUnrealizedProfitLoss(), trade.getCloseTime());
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public int getInitialUnits() {
        return initialUnits;
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

    public long getCurrentPrice() { // Assumes full trade closes
        return price + ((closeTime == null ? unrealizedProfitLoss : realizedProfitLoss) / initialUnits);
    }

    public long getPurchaseValue() {
        return price * currentUnits;
    }

    public long getNetAssetValue() {
        return getCurrentPrice() * getInitialUnits();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeSummary that = (TradeSummary) o;
        return price == that.price &&
                initialUnits == that.initialUnits &&
                currentUnits == that.currentUnits &&
                realizedProfitLoss == that.realizedProfitLoss &&
                unrealizedProfitLoss == that.unrealizedProfitLoss &&
                Objects.equals(id, that.id) &&
                instrument == that.instrument &&
                Objects.equals(openTime, that.openTime) &&
                Objects.equals(closeTime, that.closeTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, instrument, price, openTime, initialUnits, currentUnits, realizedProfitLoss, unrealizedProfitLoss, closeTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("instrument", instrument)
                .add("price", doubleFromPippetes(price))
                .add("currentPrice", doubleFromPippetes(getCurrentPrice()))
                .add("openTime", formatTimestamp(openTime))
                .add("initialUnits", initialUnits)
                .add("currentUnits", currentUnits)
                .add("realizedProfitLoss", profitLossDisplay(realizedProfitLoss))
                .add("unrealizedProfitLoss", profitLossDisplay(unrealizedProfitLoss))
                .add("closeTime", closeTime == null ? null : formatTimestamp(closeTime))
                .toString();
    }

    private TradeSummary stateChanged(CalculatedTradeState calculatedTradeState) {
        checkState(id.equals(calculatedTradeState.getId()));

        long newUnrealizedProfitLoss = calculatedTradeState.getUnrealizedProfitLoss();

        return new TradeSummary(id, instrument, price, openTime, initialUnits, currentUnits, realizedProfitLoss, newUnrealizedProfitLoss, closeTime);
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
