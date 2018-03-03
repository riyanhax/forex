package broker;

import broker.Quote;
import market.forex.ForexPortfolioValue;
import market.forex.ForexPosition;
import market.forex.Instrument;
import trader.forex.ForexTrader;

import javax.annotation.Nullable;
import java.time.LocalDate;

public interface ForexBroker {
    ForexPortfolioValue getPortfolioValue(ForexTrader trader);

    Quote getQuote(Instrument pair);

    boolean isOpen();

    boolean isOpen(LocalDate time);

    void openPosition(ForexTrader trader, Instrument pair, @Nullable Double limit);

    void closePosition(ForexTrader trader, ForexPosition position, @Nullable Double limit);
}
