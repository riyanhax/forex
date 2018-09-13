package broker;

import com.google.common.collect.Range;
import market.AccountSnapshot;
import market.Instrument;
import trader.ForexTrader;

import javax.annotation.Nullable;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.NavigableMap;

public interface ForexBroker {

    EnumSet<DayOfWeek> ALWAYS_OPEN_DAYS = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY);

    void processUpdates();

    AccountSnapshot getAccountSnapshot(ForexTrader trader) throws Exception;

    Quote getQuote(ForexTrader trader, Instrument pair) throws Exception;

    boolean isClosed();

    void openPosition(ForexTrader trader, OpenPositionRequest request) throws Exception;

    void closePosition(ForexTrader trader, TradeSummary position, @Nullable Long limit) throws Exception;

    /**
     * Retrieve one day candles based on an inclusive start and exclusive end.
     */
    NavigableMap<LocalDateTime, CandlestickData> getOneDayCandles(ForexTrader trader, Instrument pair, Range<LocalDateTime> closed) throws RequestException;

    /**
     * Retrieve four hour candles based on an inclusive start and exclusive end.
     */
    NavigableMap<LocalDateTime, CandlestickData> getFourHourCandles(ForexTrader trader, Instrument pair, Range<LocalDateTime> closed) throws RequestException;

    /**
     * Retrieve one week candles based on an inclusive start and exclusive end.
     */
    NavigableMap<LocalDateTime, CandlestickData> getOneWeekCandles(ForexTrader trader, Instrument pair, Range<LocalDateTime> closed) throws RequestException;
}
