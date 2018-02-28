package trader;

import broker.forex.ForexBroker;
import broker.forex.ForexPortfolio;
import broker.forex.ForexPortfolioValue;
import broker.forex.ForexPositionValue;
import market.forex.Instrument;
import market.order.OrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import simulator.Simulation;
import simulator.SimulatorClock;
import trader.forex.ForexTrader;

import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Collections.emptySortedSet;

class DoNothingTrader implements ForexTrader {

    private static final Logger LOG = LoggerFactory.getLogger(DoNothingTrader.class);
    private final SimulatorClock clock;
    private final Random random = new Random();

    private String accountNo = UUID.randomUUID().toString();
    private ForexPortfolio portfolio;
    private SortedSet<ForexPortfolioValue> portfolioSnapshots = new TreeSet<>();

    @Autowired
    public DoNothingTrader(SimulatorClock clock) {
        this.clock = clock;
    }

    @Override
    public String getAccountNumber() {
        return accountNo;
    }

    @Override
    public void processUpdates(ForexBroker broker) {

        ForexPortfolioValue portfolio = broker.getPortfolioValue(this);
        Set<ForexPositionValue> positions = portfolio.getPositionValues();

        boolean stopTrading = clock.now().getHour() > 11 && !broker.isOpen(clock.tomorrow());

        if (positions.isEmpty()) {
            if (!stopTrading) {
                Instrument[] instruments = Instrument.values();
                Instrument pair = instruments[random.nextInt(instruments.length)];

                broker.openPosition(this, pair, null);
            }
        } else {
            ForexPositionValue positionValue = positions.iterator().next();
            double pipsProfit = positionValue.pips();

            // Close once we've lost or gained enough pips or if it's noon Friday
            if (pipsProfit < -30 || pipsProfit > 60 || stopTrading) {
                broker.closePosition(this, positionValue.getPosition(), null);
            }
        }
    }

    @Override
    public void cancelled(OrderRequest cancelled) {

    }

    @Override
    public void init(Simulation simulation) {
        this.portfolio = new ForexPortfolio(0, emptySet(), emptySortedSet());
    }

    @Override
    public ForexPortfolio getPortfolio() {
        return portfolio;
    }

    @Override
    public void setPortfolio(ForexPortfolio portfolio) {
        this.portfolio = portfolio;
    }

    @Override
    public void addPortfolioValueSnapshot(ForexPortfolioValue portfolioValue) {
        portfolioSnapshots.add(portfolioValue);
    }

    @Override
    public SortedSet<ForexPortfolioValue> portfolioSnapshots() {
        return portfolioSnapshots;
    }

}
