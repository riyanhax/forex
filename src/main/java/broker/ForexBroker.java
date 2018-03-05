package broker;

import market.ForexPortfolioValue;
import market.ForexPosition;
import market.Instrument;
import trader.ForexTrader;

import javax.annotation.Nullable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;

public interface ForexBroker {

    EnumSet<DayOfWeek> ALWAYS_OPEN_DAYS = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY);

    void processUpdates() throws Exception;

    ForexPortfolioValue getPortfolioValue(ForexTrader trader) throws Exception;

    Quote getQuote(Instrument pair) throws Exception;

    boolean isClosed();

    boolean isClosed(LocalDate time);

    void openPosition(ForexTrader trader, OpenPositionRequest request) throws Exception;

    void closePosition(ForexTrader trader, ForexPosition position, @Nullable Double limit) throws Exception;

}
