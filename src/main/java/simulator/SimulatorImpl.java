package simulator;

import broker.forex.ForexBroker;
import broker.forex.ForexPortfolio;
import broker.forex.ForexPortfolioValue;
import broker.forex.ForexPositionValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trader.Trader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
class SimulatorImpl implements Simulator {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatorImpl.class);

    private final List<ForexBroker> brokers;
    private final List<Trader> traders;
    private final SimulatorClockImpl clock;

    @Autowired
    public SimulatorImpl(SimulatorClockImpl clock,
                         List<ForexBroker> brokers,
                         List<Trader> traders) {
        this.clock = clock;
        this.brokers = brokers;
        this.traders = traders;
    }

    @Override
    public void run(Simulation simulation) {
        init(simulation);

        while (clock.now().isBefore(simulation.endTime)) {
            nextMinute(simulation);
        }

        traders.forEach(trader -> {
            SortedSet<ForexPortfolioValue> snapshots = trader.portfolioSnapshots();
            LOG.info("End: {}", snapshots.last());

            SortedSet<ForexPortfolioValue> sortedByProfit = new TreeSet<>(Comparator.comparing(ForexPortfolioValue::pips));
            sortedByProfit.addAll(snapshots);

            LOG.info("Largest drawdown: {}", sortedByProfit.first());
            LOG.info("Highest profit: {}", sortedByProfit.last());

            ForexPortfolioValue end = snapshots.last();
            ForexPortfolio portfolio = end.getPortfolio();

            SortedSet<ForexPositionValue> tradesSortedByProfit = new TreeSet<>(Comparator.comparing(ForexPositionValue::pips));
            tradesSortedByProfit.addAll(portfolio.getClosedTrades());
            LOG.info("Worst trade: {}", tradesSortedByProfit.first());
            LOG.info("Best trade: {}", tradesSortedByProfit.last());
        });
    }

    void init(Simulation simulation) {
        clock.init(simulation.startTime);
        brokers.forEach(it -> it.init(simulation));
        traders.forEach(it -> it.init(simulation));
        brokers.forEach(ForexBroker::processUpdates);
    }

    void nextMinute(Simulation simulation) {
        LocalDateTime previous = clock.now();
        if (!previous.isBefore(simulation.endTime)) {
            throw new IllegalStateException("Can't advance beyond the end of the simulation!");
        }

        clock.advance(1, MINUTES);

        LocalDateTime now = clock.now();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Time: {}", now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
        } else {
            if (now.getHour() == 0 && now.getMinute() == 0 && now.getSecond() == 0) {
                LOG.info("Time: {}", now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
            }
        }

        brokers.forEach(ForexBroker::processUpdates);

        if (simulation.millisDelayBetweenMinutes > 0) {
            try {
                Thread.sleep(simulation.millisDelayBetweenMinutes);
            } catch (InterruptedException e) {
                LOG.error("Interrupted trying to wait!", e);
            }
        }
    }
}
