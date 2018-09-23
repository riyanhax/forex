package forex.broker;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import forex.market.Instrument;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static forex.broker.Quote.doubleFromPippetes;
import static forex.broker.Quote.profitLossDisplay;
import static forex.market.MarketTime.formatTimestamp;

@Entity(name = "trade_summary")
public class TradeSummary {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "trade_id", nullable = false)
    private String tradeId;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(nullable = false)
    private Instrument instrument;

    @Column(nullable = false)
    private long price;

    @Column(name = "open_time", nullable = false)
    private LocalDateTime openTime;

    @Column(name = "initial_units", nullable = false)
    private int initialUnits;

    @Column(name = "current_units", nullable = false)
    private int currentUnits;

    @Column(name = "realized_profit_loss", nullable = false)
    private long realizedProfitLoss;

    @Column(name = "unrealized_profit_loss", nullable = false)
    private long unrealizedProfitLoss;

    @Column(name = "close_time")
    private LocalDateTime closeTime;

    public TradeSummary() {
    }

    public TradeSummary(String tradeId, String accountId, Instrument instrument, long price, LocalDateTime openTime, int initialUnits, int currentUnits, long realizedProfitLoss, long unrealizedProfitLoss, LocalDateTime closeTime) {
        this.tradeId = tradeId;
        this.accountId = accountId;
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
        this(trade.getTradeId(), trade.getAccountId(), trade.getInstrument(), trade.getPrice(), trade.getOpenTime(), trade.getInitialUnits(),
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

    public String getTradeId() {
        return tradeId;
    }

    public String getAccountId() {
        return accountId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
        checkState(tradeId.equals(calculatedTradeState.getId()));

        long newUnrealizedProfitLoss = calculatedTradeState.getUnrealizedProfitLoss();

        return new TradeSummary(tradeId, accountId, instrument, price, openTime, initialUnits, currentUnits, realizedProfitLoss, newUnrealizedProfitLoss, closeTime);
    }

    static List<TradeSummary> incorporateState(List<TradeSummary> trades, AccountChangesState stateChanges) {
        List<CalculatedTradeState> tradeStates = stateChanges.getTrades();
        if (tradeStates.isEmpty()) {
            return trades;
        }

        ImmutableMap<String, CalculatedTradeState> stateByTradeId = Maps.uniqueIndex(tradeStates, CalculatedTradeState::getId);
        ImmutableMap<String, TradeSummary> tradesById = Maps.uniqueIndex(trades, TradeSummary::getTradeId);

        checkState(stateByTradeId.size() == tradesById.size(), "Trade and state count were different!");

        List<TradeSummary> updated = new ArrayList<>(trades.size());
        tradesById.forEach((id, trade) -> updated.add(trade.stateChanged(stateByTradeId.get(id))));

        return updated;
    }
}
