package trader;

import broker.Quote;
import broker.forex.ForexBroker;
import broker.forex.ForexPortfolio;
import broker.forex.ForexPortfolioValue;
import broker.forex.ForexPositionValue;
import market.forex.Instrument;
import market.order.OrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import simulator.Simulation;
import simulator.SimulatorClock;
import trader.forex.ForexTrader;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Collections.emptySortedSet;

@Service
class DoNothingTrader implements ForexTrader {

    private static final Logger LOG = LoggerFactory.getLogger(DoNothingTrader.class);
    private final SimulatorClock clock;

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
        LOG.info("\tChecking portfolio");

        ForexPortfolioValue portfolio = broker.getPortfolioValue(this);
        double profit = portfolio.getPipsProfit();
        Set<ForexPositionValue> positions = portfolio.getPositionValues();

        LOG.info("\tPips: {}", profit);
        LOG.info("\tPositions: {}", positions);

        Instrument pair = Instrument.EURUSD;
        Quote quote = broker.getQuote(pair);
        LOG.info("\t{} bid: {}, ask: {}", pair.getName(), quote.getBid(), quote.getAsk());

        if (positions.isEmpty()) {

            LOG.info("\tMaking orders");
            broker.openPosition(this, pair, null);
        } else {
            ForexPositionValue positionValue = positions.iterator().next();
            double pipsProfit = positionValue.pips();

            // Close once we've lost or gained enough pips or if it's noon Friday
            LocalDateTime now = clock.now();
            boolean fridayAtNoon = now.getDayOfWeek() == DayOfWeek.FRIDAY && now.getHour() == 12;
            if (pipsProfit < -30 || pipsProfit > 60 || fridayAtNoon) {
                broker.closePosition(this, positionValue.getPosition(), null);
            }
        }

        LOG.info("\tClosing orders");
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
