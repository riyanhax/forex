package broker;

import market.ForexPortfolioValue;
import market.ForexPosition;
import market.Instrument;
import trader.ForexTrader;

import javax.annotation.Nullable;
import java.time.LocalDate;

public interface ForexBroker {
    void processUpdates();

    ForexPortfolioValue getPortfolioValue(ForexTrader trader);

    Quote getQuote(Instrument pair);

    boolean isOpen();

    boolean isOpen(LocalDate time);

    void openPosition(ForexTrader trader, Instrument pair, @Nullable Double limit);

    void closePosition(ForexTrader trader, ForexPosition position, @Nullable Double limit);

}
