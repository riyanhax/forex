package broker;

import market.ForexMarket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import trader.Trader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
class Oanda implements Broker {

    private static final Logger LOG = LoggerFactory.getLogger(Oanda.class);

    private final ForexMarket forexMarket;
    private final List<Trader> traders;
    // TODO DPJ: Make this configurable, maybe in the simulation
    private final double spread = 2 * .0001d;

    public Oanda(ForexMarket forexMarket, List<Trader> traders) {
        this.forexMarket = forexMarket;
        this.traders = traders;
    }

    @Override
    public void processUpdates() {
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
    public Quote getQuote(Instrument instrument) {
        double price = forexMarket.getPrice(instrument);

        return new QuoteImpl(price - (spread / 2), price + (spread / 2));
    }
}
