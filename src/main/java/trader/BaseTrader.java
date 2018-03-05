package trader;

import broker.ForexBroker;
import broker.OpenPositionRequest;
import market.ForexPortfolio;
import market.ForexPortfolioValue;
import market.ForexPositionValue;
import market.InstrumentHistoryService;
import market.MarketTime;
import market.order.OrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

abstract class BaseTrader implements ForexTrader {

    private static final Logger LOG = LoggerFactory.getLogger(BaseTrader.class);

    private final MarketTime clock;
    private final InstrumentHistoryService instrumentHistoryService;

    private String accountNo = UUID.randomUUID().toString();
    private ForexPortfolio portfolio;

    private ForexPortfolioValue drawdownPortfolio = null;
    private ForexPortfolioValue profitPortfolio = null;
    private ForexPortfolioValue mostRecentPortfolio = null;

    // This should be managed in the market
    private OpenPositionRequest openedPosition;

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

        LocalDateTime now = clock.now();
        boolean stopTrading = now.getHour() > 11 && broker.isClosed(clock.tomorrow());

        if (positions.isEmpty()) {
            if (!stopTrading) {
                Optional<OpenPositionRequest> toOpen = shouldOpenPosition(clock, instrumentHistoryService);
                toOpen.ifPresent(request -> {
                    LOG.info("Opening position: {}", request);
                    try {
                        broker.openPosition(this, request);
                    } catch (Exception e) {
                        LOG.error("Unable to open position!", e);
                    }
                });
            }
        } else {
            ForexPositionValue positionValue = positions.iterator().next();

            LOG.info("Existing position: {}", positionValue);

            // Close if it's noon Friday
            if (stopTrading) {
                LOG.info("Closing position since it's {}", MarketTime.formatTimestamp(now));

                broker.closePosition(this, positionValue.getPosition(), null);
            }
        }
    }

    abstract Optional<OpenPositionRequest> shouldOpenPosition(MarketTime clock, InstrumentHistoryService instrumentHistoryService);

    @Override
    public void cancelled(OrderRequest cancelled) {

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

    @Override
    public OpenPositionRequest getOpenedPosition() {
        return openedPosition;
    }

    @Override
    public void setOpenedPosition(OpenPositionRequest positionRequest) {
        this.openedPosition = positionRequest;
    }
}
