package broker;

import market.ForexPortfolioValue;
import market.ForexPosition;
import market.Instrument;
import trader.ForexTrader;

import javax.annotation.Nullable;
import java.time.LocalDate;

public interface ForexBroker {
    void processUpdates() throws Exception;

    ForexPortfolioValue getPortfolioValue(ForexTrader trader) throws Exception;

    Quote getQuote(Instrument pair) throws Exception;

    boolean isClosed();

    boolean isClosed(LocalDate time);

    void openPosition(ForexTrader trader, Instrument pair, @Nullable Double limit) throws Exception;

    void closePosition(ForexTrader trader, ForexPosition position, @Nullable Double limit);

}
