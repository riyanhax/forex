package live;

import broker.Account;
import broker.AccountChanges;
import broker.AccountChangesRequest;
import broker.AccountChangesResponse;
import broker.AccountID;
import broker.Context;
import broker.RequestException;
import broker.TradeListRequest;
import broker.TradeListResponse;
import broker.TradeSummary;
import broker.TransactionID;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Stopwatch;
import market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import trader.BaseTrader;
import trader.TradingStrategy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;

public class OandaTrader extends BaseTrader {

    private static final Logger LOG = LoggerFactory.getLogger(OandaTrader.class);

    private final String accountId;
    private final Context ctx;
    private Account account;
    private SortedSet<TradeSummary> lastTenClosedTrades = new TreeSet<>(comparing(TradeSummary::getOpenTime));

    public OandaTrader(String accountId, Context ctx, TradingStrategy tradingStrategy, MarketTime clock) {
        super(tradingStrategy, clock);
        this.accountId = accountId;
        this.ctx = ctx;

        refresh();
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
        if (null == account) {
            refresh();
        } else {
            refreshAccount();
        }

        return Optional.ofNullable(account);
    }

    @Override
    public Optional<TradeSummary> getLastClosedTrade() {
        if (null == account) {
            refresh();
        } else {
             refreshAccount();
        }

        return lastTenClosedTrades.isEmpty() ? Optional.empty() : Optional.of(lastTenClosedTrades.last());
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

            List<TradeSummary> trades = this.account.getTrades();
            long profitLoss = this.account.getPl();

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

                Set<String> closedTradeIds = tradesClosed.stream().map(TradeSummary::getId).collect(toSet());

                trades = new ArrayList<>(trades);
                trades.removeIf(it -> closedTradeIds.contains(it.getId()));

                profitLoss += tradesClosed.stream().mapToLong(TradeSummary::getRealizedProfitLoss).sum();
            }

            if (!tradesOpened.isEmpty()) {
                trades = new ArrayList<>(trades);
                trades.addAll(tradesOpened);
            }

            this.account = new Account(this.account.getId(), mostRecentTransactionID, trades, profitLoss);
        }
    }

    private void refresh() {
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
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("accountId", accountId);
    }
}
