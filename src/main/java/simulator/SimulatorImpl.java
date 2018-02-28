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
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static simulator.SimulatorClock.formatRange;
import static simulator.SimulatorClock.formatTimestamp;

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
        Map<ForexTraderFactory, Collection<ForexTrader>> tradersByFactory = new IdentityHashMap<>();
        traderFactories.forEach(it -> tradersByFactory.put(it, it.createInstances(simulation)));

        List<ForexTrader> allTraders = tradersByFactory.values().stream().flatMap(Collection::stream).collect(toList());
        init(simulation, allTraders);

        while (clock.now().isBefore(simulation.endTime)) {
            nextMinute(simulation);
        }

        tradersByFactory.forEach((factory, traders) -> {

            LOG.info("\n\n{}:", factory.getClass().getName());

            double averageProfit = 0d;

            SortedSet<ForexPortfolioValue> portfolios = new TreeSet<>(Comparator.comparing(ForexPortfolioValue::pips));
            SortedSet<ForexPositionValue> tradesSortedByProfit = new TreeSet<>(Comparator.comparing(ForexPositionValue::pips));

            for (ForexTrader trader : traders) {
                ForexPortfolioValue end = trader.getMostRecentPortfolio();
                double endPips = end.pips();
                LOG.info("End: {} pips at {}", endPips, formatTimestamp(end.getTimestamp()));

                averageProfit += endPips;

                portfolios.add(trader.getDrawdownPortfolio());
                portfolios.add(trader.getProfitPortfolio());

                ForexPortfolio portfolio = end.getPortfolio();
                tradesSortedByProfit.addAll(portfolio.getClosedTrades());
            }

            averageProfit /= traders.size();

            ForexPositionValue worstTrade = tradesSortedByProfit.first();
            ForexPositionValue bestTrade = tradesSortedByProfit.last();

            ForexPortfolioValue drawdownPortfolio = portfolios.first();
            ForexPortfolioValue profitPortfolio = portfolios.last();

            LOG.info("Worst trade: {} pips from {}", worstTrade.pips(), formatRange(worstTrade.getPosition().getOpened(), worstTrade.getTimestamp()));
            LOG.info("Best trade: {} pips from {}", bestTrade.pips(), formatRange(bestTrade.getPosition().getOpened(), bestTrade.getTimestamp()));
            LOG.info("Profitable trades: {}/{}", tradesSortedByProfit.stream().filter(it -> it.pips() > 0).count(), tradesSortedByProfit.size());
            LOG.info("Highest drawdown: {} pips at {}", drawdownPortfolio.pips(), formatTimestamp(drawdownPortfolio.getTimestamp()));
            LOG.info("Highest profit: {} pips at {}", profitPortfolio.pips(), formatTimestamp(profitPortfolio.getTimestamp()));
            LOG.info("Average profit: {} pips from {}", averageProfit, formatRange(simulation.startTime, simulation.endTime));
        });
    }

    void init(Simulation simulation, Collection<ForexTrader> traders) {
        clock.init(simulation.startTime);

        brokers.forEach(it -> it.init(simulation, traders));
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
            LOG.debug("Time: {}", formatTimestamp(now));
        } else {
            if (now.getHour() == 0 && now.getMinute() == 0 && now.getSecond() == 0) {
                LOG.info("Time: {}", formatTimestamp(now));
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
