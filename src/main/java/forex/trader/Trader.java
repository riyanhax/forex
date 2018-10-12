package forex.trader;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import forex.broker.AccountAndTrades;
import forex.broker.AccountSummary;
import forex.broker.Context;
import forex.broker.ForexBroker;
import forex.broker.OpenPositionRequest;
import forex.broker.Orders;
import forex.broker.RequestException;
import forex.broker.TradeSummary;
import forex.market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.time.DayOfWeek.FRIDAY;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class Trader implements ForexTrader {

    private static final Logger LOG = LoggerFactory.getLogger(Trader.class);

    private final String accountId;
    private final Context ctx;
    private final TraderService traderService;
    private final TradingStrategy tradingStrategy;
    private final MarketTime clock;

    private AccountSummary account;
    private SortedSet<TradeSummary> lastTenClosedTrades = closedTrades();

    public Trader(String accountId, Context ctx, TraderService traderService, TradingStrategy tradingStrategy, MarketTime clock) {
        this.accountId = accountId;
        this.traderService = traderService;
        this.ctx = ctx;
        this.tradingStrategy = tradingStrategy;
        this.clock = clock;
    }

    @Override
    public TradingStrategy getStrategy() {
        return tradingStrategy;
    }

    @Override
    public void processUpdates(ForexBroker broker) throws Exception {

        LOG.info("Trader: {} ({})", tradingStrategy.getName(), accountId);
        refreshDataSinceLastInterval();

        if (account == null) {
            LOG.error("No account available! Skipping this interval...");
            return;
        }

        List<TradeSummary> positions = account.getTrades();

        LocalDateTime now = clock.now();
        boolean stopTrading = now.getHour() > 11 && now.getDayOfWeek() == FRIDAY;

        if (!positions.isEmpty()) {
            TradeSummary positionValue = positions.iterator().next();

            LOG.info("Existing position: {}", positionValue);

            // Close if it's noon Friday
            if (stopTrading) {
                LOG.info("Closing position since it's {}", MarketTime.formatTimestamp(now));

                broker.closePosition(this, positionValue, null);
            }

            return;
        }

        if (stopTrading) {
            return;
        }

        Orders pendingOrders = account.getPendingOrders();
        if (!(pendingOrders.getMarketOrders().isEmpty() && pendingOrders.getLimitOrders().isEmpty())) {
            LOG.info("Pending orders exist: {}", pendingOrders);
            return;
        }

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

    private AccountAndTrades refreshDataSinceLastInterval() {
        Stopwatch timer = Stopwatch.createStarted();

        try {
            AccountAndTrades accountAndLastTenTrades = traderService.accountAndTrades(this.accountId, 10);
            this.account = accountAndLastTenTrades.getAccount();
            this.lastTenClosedTrades.clear();
            this.lastTenClosedTrades.addAll(accountAndLastTenTrades.getTrades().stream()
                    .map(TradeSummary::new).collect(toList()));

            LOG.info("Loaded account {} and {} closed trades in {}", accountId, lastTenClosedTrades.size(), timer);

            return accountAndLastTenTrades;
        } catch (RequestException e) {
            LOG.error("Unable to retrieve the account and closed trades!", e);
        }
        return null;
    }

    @Override
    public String getAccountNumber() {
        return accountId;
    }

    @Override
    public Context getContext() {
        return ctx;
    }

    @Override
    public Optional<AccountSummary> getAccount() {
        return Optional.ofNullable(account);
    }

    @Override
    public Optional<TradeSummary> getLastClosedTrade() {
        return lastTenClosedTrades.isEmpty() ? Optional.empty() : Optional.of(lastTenClosedTrades.last());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("accountId", accountId)
                .add("tradingStrategy", tradingStrategy)
                .toString();
    }

    private static SortedSet<TradeSummary> closedTrades() {
        return new TreeSet<>(comparing(TradeSummary::getOpenTime));
    }
}
