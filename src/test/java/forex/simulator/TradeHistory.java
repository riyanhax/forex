package forex.simulator;

import forex.broker.CandlestickData;
import forex.broker.TradeSummary;
import com.google.common.base.MoreObjects;
import forex.market.Instrument;

import java.time.LocalDateTime;
import java.util.NavigableMap;
import java.util.Objects;

public class TradeHistory {

    private final TradeSummary trade;
    private final NavigableMap<LocalDateTime, CandlestickData> candles;

    TradeHistory(TradeSummary trade, NavigableMap<LocalDateTime, CandlestickData> candles) {
        this.trade = trade;
        this.candles = candles;
    }

    public TradeSummary getTrade() {
        return trade;
    }

    public NavigableMap<LocalDateTime, CandlestickData> getCandles() {
        return candles;
    }

    public long getRealizedProfitLoss() {
        return trade.getRealizedProfitLoss();
    }

    public LocalDateTime getOpenTime() {
        return trade.getOpenTime();
    }

    public LocalDateTime getCloseTime() {
        return trade.getCloseTime();
    }

    public String getId() {
        return trade.getId();
    }

    public Instrument getInstrument() {
        return trade.getInstrument();
    }

    public int getInitialUnits() {
        return trade.getInitialUnits();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeHistory that = (TradeHistory) o;
        return Objects.equals(trade, that.trade) &&
                Objects.equals(candles, that.candles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trade, candles);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("trade", trade)
                .add("candles", candles)
                .toString();
    }
}
