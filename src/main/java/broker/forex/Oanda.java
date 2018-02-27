package broker.forex;

import broker.BidAsk;
import broker.Position;
import broker.PositionValue;
import broker.Quote;
import market.MarketEngine;
import market.forex.Instrument;
import market.order.OrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import simulator.Simulation;
import simulator.SimulatorClock;
import trader.Trader;
import trader.forex.ForexTrader;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
class Oanda implements ForexBroker {

    private static final Logger LOG = LoggerFactory.getLogger(Oanda.class);

    private final SimulatorClock clock;
    private final MarketEngine marketEngine;
    private final List<ForexTrader> traders;
    private final Map<String, ForexPortfolio> portfoliosByAccountNumber = new HashMap<>();
    private final Map<String, Trader> tradersByOrderId = new HashMap<>();
    private Simulation simulation;

    public Oanda(SimulatorClock clock, MarketEngine marketEngine, List<ForexTrader> traders) {
        this.clock = clock;
        this.marketEngine = marketEngine;
        this.traders = traders;
    }

    @Override
    public void init(Simulation simulation) {
        this.simulation = simulation;

        marketEngine.init(simulation);

        tradersByOrderId.clear();
        portfoliosByAccountNumber.clear();
        traders.forEach(it -> portfoliosByAccountNumber.put(it.getAccountNumber(),
                new ForexPortfolio(simulation.portfolioDollars, Collections.emptySet())));
    }

    @Override
    public void processUpdates() {

        marketEngine.processUpdates();

        if (isOpen()) {
            LOG.info("\tCheck pending orders");
            LOG.info("\tProcess transactions");

            traders.forEach(it -> it.processUpdates(this));
        }
    }

    @Override
    public ForexPortfolioValue getPortfolio(Trader trader) {
        ForexPortfolio portfolio = portfoliosByAccountNumber.get(trader.getAccountNumber());

        Set<Position> positions = portfolio.getPositions();
        Set<PositionValue> positionValues = positions.stream()
                .map(it -> {
                    double price = marketEngine.getPrice(it.getInstrument());
                    return new ForexPositionValue(it, price);
                })
                .collect(Collectors.toSet());

        return new ForexPortfolioValue(portfolio, clock.now(), positionValues);
    }

    @Override
    public Quote getQuote(Instrument pair) {
        double price = marketEngine.getPrice(pair);
        double halfSpread = (simulation.pipSpread * pair.getPip()) / 2;

        return new BidAsk(price - halfSpread, price + halfSpread);
    }

    @Override
    public void orderFilled(OrderRequest filled) {
        double cashValue = filled.getExecutionPrice() * filled.getUnits();

        Trader trader = tradersByOrderId.get(filled.getId());
        ForexPortfolio oldPortfolio = portfoliosByAccountNumber.get(trader.getAccountNumber());
        HashSet<Position> newPositions = new HashSet<>(oldPortfolio.getPositions());
        if (filled.isSellOrder()) {
            newPositions.removeIf(it -> it.getInstrument() == filled.getInstrument());
        } else if(filled.isBuyOrder()) {
            newPositions.add(new ForexPosition(filled.getInstrument(), filled.getUnits()));
        }

        double cash = oldPortfolio.getCash() - cashValue;
        ForexPortfolio portfolio = new ForexPortfolio(cash, newPositions);

        portfoliosByAccountNumber.put(trader.getAccountNumber(), portfolio);
    }

    @Override
    public boolean isOpen() {
        return marketEngine.isAvailable();
    }


}
