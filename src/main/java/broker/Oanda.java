package broker;

import instrument.CurrencyPair;
import market.ForexMarket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import trader.Trader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
class Oanda implements Broker<CurrencyPair, ForexMarket> {

    private static final Logger LOG = LoggerFactory.getLogger(Oanda.class);

    private final ForexMarket forexMarket;
    private final List<Trader> traders;
    // TODO DPJ: Make this configurable, maybe in the simulation
    private final double pipSpread = 2;

    public Oanda(ForexMarket forexMarket, List<Trader> traders) {
        this.forexMarket = forexMarket;
        this.traders = traders;
    }

    @Override
    public void processUpdates() {

        if (!forexMarket.isAvailable()) {
            LOG.info("Broker is closed.");
            return;
        }

        forexMarket.processUpdates();

        LOG.info("\tCheck pending orders");
        LOG.info("\tProcess transactions");

        traders.forEach(it -> it.processUpdates(this));
    }

    @Override
    public Portfolio getPortfolio(Trader trader) {
        // TODO DPJ: Cache portfolios per trader
        Set<PositionValue> positionValues = new HashSet<>();

        return new PortfolioImpl(positionValues);
    }

    @Override
    public Quote getQuote(CurrencyPair pair) {
        double price = forexMarket.getPrice(pair);
        double halfSpread = (pipSpread * pair.getPip()) / 2;

        return new QuoteImpl(price - halfSpread, price + halfSpread);
    }
}
