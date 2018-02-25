package broker.forex;

import broker.BidAsk;
import broker.Broker;
import broker.Quote;
import market.forex.CurrencyPair;
import market.forex.ForexMarket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import simulator.SimulatorClock;
import trader.Trader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
class Oanda implements Broker<CurrencyPair, ForexMarket, ForexPosition, ForexPositionValue, ForexPortfolioValue> {

    private static final Logger LOG = LoggerFactory.getLogger(Oanda.class);

    private final SimulatorClock clock;
    private final ForexMarket forexMarket;
    private final List<Trader> traders;
    // TODO DPJ: Make this configurable, maybe in the simulation
    private final double pipSpread = 2;

    public Oanda(SimulatorClock clock, ForexMarket forexMarket, List<Trader> traders) {
        this.clock = clock;
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
    public ForexPortfolioValue getPortfolio(Trader trader) {
        // TODO DPJ: Cache portfolios per trader
        Set<ForexPosition> positions = new HashSet<>();
        long cash = System.currentTimeMillis() * 100;

        ForexPortfolio portfolio = new ForexPortfolio(cash, positions);
        Set<ForexPositionValue> positionValues = positions.stream()
                .map(it -> {
                    double price = forexMarket.getPrice(it.getInstrument());
                    return new ForexPositionValue(it, price);
                })
                .collect(Collectors.toSet());

        return new ForexPortfolioValue(portfolio, clock.now(), positionValues);
    }

    @Override
    public Quote getQuote(CurrencyPair pair) {
        double price = forexMarket.getPrice(pair);
        double halfSpread = (pipSpread * pair.getPip()) / 2;

        return new BidAsk(price - halfSpread, price + halfSpread);
    }
}
