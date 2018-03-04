package live;

import broker.Quote;
import broker.ForexBroker;
import market.forex.ForexPortfolioValue;
import market.forex.ForexPosition;
import market.forex.Instrument;
import org.springframework.stereotype.Service;
import trader.forex.ForexTrader;

import javax.annotation.Nullable;
import java.time.LocalDate;

@Service
class Oanda implements ForexBroker {

    @Override
    public ForexPortfolioValue getPortfolioValue(ForexTrader trader) {
        return null;
    }

    @Override
    public Quote getQuote(Instrument pair) {
        return null;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isOpen(LocalDate time) {
        return false;
    }

    @Override
    public void openPosition(ForexTrader trader, Instrument pair, @Nullable Double limit) {

    }

    @Override
    public void closePosition(ForexTrader trader, ForexPosition position, @Nullable Double limit) {

    }

    @Override
    public void processUpdates() {

    }
}
