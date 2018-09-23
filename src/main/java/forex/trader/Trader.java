package forex.trader;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import forex.broker.Account;
import forex.broker.AccountAndTrades;
import forex.broker.AccountChangesRequest;
import forex.broker.AccountChangesResponse;
import forex.broker.Context;
import forex.broker.ForexBroker;
import forex.broker.OpenPositionRequest;
import forex.broker.RequestException;
import forex.broker.TradeSummary;
import forex.market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Iterator;
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
    private final TradingStrategy tradingStrategy;
    private final MarketTime clock;

    private Account account;
    private SortedSet<TradeSummary> lastTenClosedTrades = closedTrades();

    public Trader(String accountId, Context ctx, TradingStrategy tradingStrategy, MarketTime clock) {
        this.accountId = accountId;
        this.ctx = ctx;
        this.tradingStrategy = tradingStrategy;
        this.clock = clock;

        initializeEverything();
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

    private void refreshDataSinceLastInterval() {
        if (null == account) {
            initializeEverything();
        } else {
            refreshAccount();
        }
    }

    private void refreshAccount() {
        String lastKnowTransactionID = account.getLastTransactionID();

        AccountChangesRequest request = new AccountChangesRequest(account.getId(), lastKnowTransactionID);

        AccountChangesResponse response;
        try {
            response = this.ctx.accountChanges(request);
        } catch (RequestException e) {
            LOG.error("Unable to check for account changes, assuming current state!", e);
            return;
        }

        this.account = this.account.processChanges(response);

        List<TradeSummary> tradesClosed = response.getAccountChanges().getTradesClosed();
        if (!tradesClosed.isEmpty()) {
            int toRemove = (lastTenClosedTrades.size() + tradesClosed.size()) - 10;
            if (toRemove > 0) {
                Iterator<TradeSummary> iter = lastTenClosedTrades.iterator();
                for (int i = 0; i < toRemove; i++) {
                    iter.next();
                    iter.remove();
                }
            }
            lastTenClosedTrades.addAll(tradesClosed);
        }
    }

    private void initializeEverything() {
        Stopwatch timer = Stopwatch.createStarted();

        try {
            AccountAndTrades accountAndLastTenTrades = ctx.initializeAccount(this.accountId, 10);
            this.account = accountAndLastTenTrades.getAccount();
            this.lastTenClosedTrades.addAll(accountAndLastTenTrades.getTrades().stream()
                    .map(TradeSummary::new).collect(toList()));

            LOG.info("Loaded account {} and {} closed trades in {}", accountId, lastTenClosedTrades.size(), timer);
        } catch (RequestException e) {
            LOG.error("Unable to retrieve the account and closed trades!", e);
        }
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
    public Optional<Account> getAccount() {
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
