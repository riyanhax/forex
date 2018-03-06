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

public abstract class BaseTrader implements ForexTrader {

    private static final Logger LOG = LoggerFactory.getLogger(BaseTrader.class);

    private final MarketTime clock;
    private final InstrumentHistoryService instrumentHistoryService;
    private final TradingStrategy tradingStrategy;

    private ForexPortfolio portfolio;

    public BaseTrader(TradingStrategy tradingStrategy, MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        this.tradingStrategy = tradingStrategy;
        this.clock = clock;
        this.instrumentHistoryService = instrumentHistoryService;
    }

    @Override
    public void processUpdates(ForexBroker broker) throws Exception {

        ForexPortfolioValue portfolio = broker.getPortfolioValue(this);
        Set<ForexPositionValue> positions = portfolio.getPositionValues();

        LocalDateTime now = clock.now();
        boolean stopTrading = now.getHour() > 11 && broker.isClosed(clock.tomorrow());

        if (positions.isEmpty()) {
            if (!stopTrading) {
                Optional<OpenPositionRequest> toOpen = tradingStrategy.shouldOpenPosition(clock, instrumentHistoryService);
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
}
