package live;

import broker.ForexBroker;
import broker.Quote;
import market.ForexPortfolioValue;
import market.ForexPosition;
import market.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import trader.ForexTrader;

import javax.annotation.Nullable;
import java.time.LocalDate;

@Service
class Oanda implements ForexBroker {

    public static final Logger LOG = LoggerFactory.getLogger(Oanda.class);
    private ForexTrader trader;

    public Oanda(ForexTrader trader) {
        this.trader = trader;
    }

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
        if (!isOpen()) {
            LOG.info("Market is closed.");
            return;
        }

        trader.processUpdates(this);
    }
}
