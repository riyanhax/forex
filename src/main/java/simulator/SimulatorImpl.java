package simulator;

import broker.forex.ForexBroker;
import broker.forex.ForexPortfolio;
import broker.forex.ForexPortfolioValue;
import broker.forex.ForexPositionValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trader.forex.ForexTrader;
import trader.forex.ForexTraderFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
class SimulatorImpl implements Simulator {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatorImpl.class);

    private final List<ForexBroker> brokers;
    private final List<ForexTraderFactory> traderFactories;
    private final SimulatorClockImpl clock;

    @Autowired
    public SimulatorImpl(SimulatorClockImpl clock,
                         List<ForexBroker> brokers,
                         List<ForexTraderFactory> traderFactories) {
        this.clock = clock;
        this.brokers = brokers;
        this.traderFactories = traderFactories;
    }

    @Override
    public void run(Simulation simulation) {
        List<ForexTrader> traders = new ArrayList<>();
        traderFactories.forEach(it -> traders.addAll(it.createInstances(simulation)));

        init(simulation, traders);

        while (clock.now().isBefore(simulation.endTime)) {
            nextMinute(simulation);
        }

        traders.forEach(trader -> {
            ForexPortfolioValue end = trader.getMostRecentPortfolio();
            LOG.info("End: {}", end);

            LOG.info("Largest drawdown: {}", trader.getDrawdownPortfolio());
            LOG.info("Highest profit: {}", trader.getProfitPortfolio());

            ForexPortfolio portfolio = end.getPortfolio();

            SortedSet<ForexPositionValue> tradesSortedByProfit = new TreeSet<>(Comparator.comparing(ForexPositionValue::pips));
            tradesSortedByProfit.addAll(portfolio.getClosedTrades());
            LOG.info("Worst trade: {}", tradesSortedByProfit.first());
            LOG.info("Best trade: {}", tradesSortedByProfit.last());
        });
    }

    void init(Simulation simulation, List<ForexTrader> traders) {
        clock.init(simulation.startTime);

        brokers.forEach(it -> it.init(simulation, traders));
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
