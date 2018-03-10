package simulator;

import broker.CandlestickData;
import broker.TradeSummary;
import market.Instrument;

import java.time.LocalDateTime;
import java.util.NavigableMap;

import static java.util.Comparator.comparing;

class TradeHistory implements Comparable<TradeHistory> {

    private final TradeSummary trade;
    private final NavigableMap<LocalDateTime, CandlestickData> candles;

    TradeHistory(TradeSummary trade, NavigableMap<LocalDateTime, CandlestickData> candles) {
        this.trade = trade;
        this.candles = candles;
    }

    TradeSummary getTrade() {
        return trade;
    }

    NavigableMap<LocalDateTime, CandlestickData> getCandles() {
        return candles;
    }

    long getRealizedProfitLoss() {
        return trade.getRealizedProfitLoss();
    }

    LocalDateTime getOpenTime() {
        return trade.getOpenTime();
    }

    LocalDateTime getCloseTime() {
        return trade.getCloseTime();
    }

    String getId() {
        return trade.getId();
    }

    Instrument getInstrument() {
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
