package forex.broker;

import com.google.common.base.MoreObjects;
import forex.market.Instrument;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static forex.broker.Quote.doubleFromPippetes;
import static forex.broker.Quote.profitLossDisplay;
import static forex.broker.TradeState.CLOSED;
import static forex.broker.TradeState.OPEN;
import static forex.market.MarketTime.formatTimestamp;
import static java.util.Collections.emptyList;

@Entity(name = "trade")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Transient
    private TradeState state;

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

    @Column(name = "margin_used", nullable = false)
    private long marginUsed;

    @Column(name = "average_close_price", nullable = false)
    private long averageClosePrice;

    @Transient
    private List<String> closingTransactionIDs;

    @Column(nullable = false)
    private long financing;

    //    com.oanda.v20.trade.Trade setClientExtensions clientExtensions;
//TakeProfitOrder takeProfitOrder
//StopLossOrder stopLossOrder
    //TrailingStopLossOrder trailingStopLossOrder

    public Trade() {
    }

    public Trade(String tradeId, String accountId, Instrument instrument, long price, LocalDateTime openTime, TradeState state, int initialUnits,
                 int currentUnits, long realizedProfitLoss, long unrealizedProfitLoss, long marginUsed, long averageClosePrice,
                 List<String> closingTransactionIDs, long financing, @Nullable LocalDateTime closeTime) {
        this.tradeId = tradeId;
        this.accountId = accountId;
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

    public Trade(Trade trade, @Nullable LocalDateTime closeTime) {
        this(trade.getTradeId(), trade.getAccountId(), trade.getInstrument(), trade.getPrice(), trade.getOpenTime(),
                trade.getCloseTime() == null ? OPEN : CLOSED, trade.getInitialUnits(), trade.getCurrentUnits(), trade.getRealizedProfitLoss(), trade.getUnrealizedProfitLoss(),
                0L, 0L, emptyList(), 0L, closeTime);

        this.id = trade.getId();
    }

    public Trade(TradeSummary summary) {
        this(summary.getTradeId(), summary.getAccountId(), summary.getInstrument(), summary.getPrice(), summary.getOpenTime(),
                summary.getCloseTime() == null ? OPEN : CLOSED, summary.getInitialUnits(), summary.getCurrentUnits(), summary.getRealizedProfitLoss(), summary.getUnrealizedProfitLoss(),
                0L, 0L, emptyList(), 0L, summary.getCloseTime());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTradeId() {
        return tradeId;
    }

    public String getAccountId() {
        return accountId;
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
                Objects.equals(tradeId, trade.tradeId) &&
                Objects.equals(accountId, trade.accountId) &&
                instrument == trade.instrument &&
                Objects.equals(openTime, trade.openTime) &&
                state == trade.state &&
                Objects.equals(closingTransactionIDs, trade.closingTransactionIDs) &&
                Objects.equals(closeTime, trade.closeTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tradeId, accountId, instrument, price, openTime, state, initialUnits, currentUnits, realizedProfitLoss, unrealizedProfitLoss, marginUsed, averageClosePrice, closingTransactionIDs, financing, closeTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("tradeId", tradeId)
                .add("accountId", accountId)
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
