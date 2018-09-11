package live;

import broker.Account;
import broker.AccountChanges;
import broker.AccountChangesRequest;
import broker.AccountChangesResponse;
import broker.AccountChangesState;
import broker.AccountID;
import broker.Context;
import broker.ForexBroker;
import broker.OpenPositionRequest;
import broker.RequestException;
import broker.TradeListRequest;
import broker.TradeListResponse;
import broker.TradeSummary;
import broker.TransactionID;
import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import trader.ForexTrader;
import trader.TradingStrategy;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import static broker.Quote.formatDollars;
import static java.time.DayOfWeek.FRIDAY;
import static java.util.Comparator.comparing;

public class Trader implements ForexTrader {

    private static final Logger LOG = LoggerFactory.getLogger(Trader.class);

    private final String accountId;
    private final Context ctx;
    private final TradingStrategy tradingStrategy;
    private final MarketTime clock;

    private Account account;
    private SortedSet<TradeSummary> lastTenClosedTrades = new TreeSet<>(comparing(TradeSummary::getOpenTime));

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
        TransactionID lastKnowTransactionID = account.getLastTransactionID();

        AccountChangesRequest request = new AccountChangesRequest(account.getId(), lastKnowTransactionID);

        AccountChangesResponse response;
        try {
            response = this.ctx.accountChanges(request);
        } catch (RequestException e) {
            LOG.error("Unable to check for account changes, assuming current state!", e);
            return;
        }

        TransactionID mostRecentTransactionID = response.getLastTransactionID();

        boolean changesExist = !mostRecentTransactionID.equals(lastKnowTransactionID);

        if (changesExist) {
            LOG.info("Changes exist: transaction id {} != {}", mostRecentTransactionID, lastKnowTransactionID);

            AccountChanges changes = response.getAccountChanges();
            List<TradeSummary> tradesClosed = changes.getTradesClosed();
            List<TradeSummary> tradesOpened = changes.getTradesOpened();

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

                for (TradeSummary closedTrade : tradesClosed) {
                    this.account = this.account.positionClosed(closedTrade, mostRecentTransactionID);
                }
            }

            for (TradeSummary openedTrade : tradesOpened) {
                this.account = this.account.positionOpened(openedTrade, mostRecentTransactionID);
            }
        }

        AccountChangesState stateChanges = response.getAccountChangesState();

        this.account = this.account.incorporateState(stateChanges);

        // Would need to consider financing charges  and probably interest for the NAV to match exactly.
        // So this is adjusting the balance when some kind of discrepancy exists.
        long brokerNetAssetValue = stateChanges.getNetAssetValue();
        long accountNetAssetValue = account.getNetAssetValue();
        long balanceAdjustment = brokerNetAssetValue == accountNetAssetValue ? 0L :
                brokerNetAssetValue - accountNetAssetValue;

        // TODO Write test for balance adjustments that occur because of financing, interest, etc.
        if (balanceAdjustment != 0) {
            account = account.adjustBalance(balanceAdjustment);
            accountNetAssetValue = account.getNetAssetValue();
        }

        LOG.info("Broker NAV: {}, Calculated NAV: {}{}, Unrealized profit: {}",
                formatDollars(brokerNetAssetValue),
                formatDollars(accountNetAssetValue),
                balanceAdjustment == 0 ? "" : String.format(" [adjusted %s]", formatDollars(balanceAdjustment)),
                formatDollars(stateChanges.getUnrealizedProfitAndLoss()));
    }

    private void initializeEverything() {
        Stopwatch timer = Stopwatch.createStarted();

        boolean initializeClosedTrades = account == null;
        try {
            account = ctx.getAccount(new AccountID(this.accountId)).getAccount();

            if (initializeClosedTrades) {
                TradeListResponse tradeListResponse = ctx.listTrade(new TradeListRequest(new AccountID(this.accountId), 10));
                lastTenClosedTrades.addAll(tradeListResponse.getTrades());
            }

            LOG.info("Loaded account {} {}in {}", accountId, initializeClosedTrades ?
                    "and closed trades " : "", timer);
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
}
