package trader;

import broker.ForexBroker;
import market.ForexPortfolio;
import market.ForexPortfolioValue;
import market.ForexPositionValue;
import market.Instrument;
import market.InstrumentHistoryService;
import market.MarketTime;
import market.order.OrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulator.Simulation;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Collections.emptySortedSet;

abstract class BaseTrader implements ForexTrader {

    private static final Logger LOG = LoggerFactory.getLogger(BaseTrader.class);

    private final MarketTime clock;
    private final InstrumentHistoryService instrumentHistoryService;

    private String accountNo = UUID.randomUUID().toString();
    private ForexPortfolio portfolio;

    private ForexPortfolioValue drawdownPortfolio = null;
    private ForexPortfolioValue profitPortfolio = null;
    private ForexPortfolioValue mostRecentPortfolio = null;

    BaseTrader(MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        this.clock = clock;
        this.instrumentHistoryService = instrumentHistoryService;
    }

    @Override
    public String getAccountNumber() {
        return accountNo;
    }

    @Override
    public void processUpdates(ForexBroker broker) throws Exception {

        ForexPortfolioValue portfolio = broker.getPortfolioValue(this);
        Set<ForexPositionValue> positions = portfolio.getPositionValues();

        boolean stopTrading = clock.now().getHour() > 11 && broker.isClosed(clock.tomorrow());

        if (positions.isEmpty()) {
            if (!stopTrading) {
                Optional<Instrument> toOpen = shouldOpenPosition(clock, instrumentHistoryService);
                toOpen.ifPresent(pair -> {
                    try {
                        broker.openPosition(this, pair, null);
                    } catch (Exception e) {
                        LOG.error("Unable to open position!", e);
                    }
                });
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

    abstract Optional<Instrument> shouldOpenPosition(MarketTime clock, InstrumentHistoryService instrumentHistoryService);

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
        if (drawdownPortfolio == null || drawdownPortfolio.pips() > portfolioValue.pips()) {
            drawdownPortfolio = portfolioValue;
        }
        if (profitPortfolio == null || profitPortfolio.pips() < portfolioValue.pips()) {
            profitPortfolio = portfolioValue;
        }
        mostRecentPortfolio = portfolioValue;
    }

    public ForexPortfolioValue getDrawdownPortfolio() {
        return drawdownPortfolio;
    }

    public ForexPortfolioValue getProfitPortfolio() {
        return profitPortfolio;
    }

    public ForexPortfolioValue getMostRecentPortfolio() {
        return mostRecentPortfolio;
    }
}
