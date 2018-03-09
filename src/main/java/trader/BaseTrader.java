package trader;

import broker.ForexBroker;
import broker.OpenPositionRequest;
import broker.TradeSummary;
import market.AccountSnapshot;
import market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public abstract class BaseTrader implements ForexTrader {

    private static final Logger LOG = LoggerFactory.getLogger(BaseTrader.class);

    private final MarketTime clock;
    private final TradingStrategy tradingStrategy;

    public BaseTrader(TradingStrategy tradingStrategy, MarketTime clock) {
        this.tradingStrategy = tradingStrategy;
        this.clock = clock;
    }

    @Override
    public TradingStrategy getStrategy() {
        return tradingStrategy;
    }

    @Override
    public void processUpdates(ForexBroker broker) throws Exception {

        AccountSnapshot portfolio = broker.getAccountSnapshot(this);
        List<TradeSummary> positions = portfolio.getPositionValues();

        LocalDateTime now = clock.now();
        boolean stopTrading = now.getHour() > 11 && broker.isClosed(clock.tomorrow());

        if (positions.isEmpty()) {
            if (!stopTrading) {
                Optional<OpenPositionRequest> toOpen = tradingStrategy.shouldOpenPosition(this, broker, clock);
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
            TradeSummary positionValue = positions.iterator().next();

            LOG.info("Existing position: {}", positionValue);

            // Close if it's noon Friday
            if (stopTrading) {
                LOG.info("Closing position since it's {}", MarketTime.formatTimestamp(now));

                broker.closePosition(this, positionValue, null);
            }
        }
    }
}
