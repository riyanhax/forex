package simulator;

import broker.CandlestickData;
import broker.TradeSummary;
import market.Instrument;

import java.time.LocalDateTime;
import java.util.NavigableMap;

import static java.util.Comparator.comparing;

public class TradeHistory implements Comparable<TradeHistory> {

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

    public int getCurrentUnits() {
        return trade.getCurrentUnits();
    }

    @Override
    public int compareTo(TradeHistory o) {
        return comparing(TradeHistory::getTrade).compare(this, o);
    }
}
