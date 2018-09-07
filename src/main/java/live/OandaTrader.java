package live;

import broker.Account;
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

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Comparator.comparing;

public class OandaTrader extends BaseTrader {

    private static final Logger LOG = LoggerFactory.getLogger(OandaTrader.class);

    private final String accountId;
    private final Context ctx;
    private Account account;
    private SortedSet<TradeSummary> lastTenClosedTrades = new TreeSet<>(comparing(TradeSummary::getOpenTime));

    public OandaTrader(String accountId, Context ctx, TradingStrategy tradingStrategy, MarketTime clock) throws Exception {
        super(tradingStrategy, clock);
        this.accountId = accountId;
        this.ctx = ctx;

        refresh();
    }

    @Override
    public String getAccountNumber() {
        return accountId;
    }

    Context getContext() {
        return ctx;
    }

    Optional<Account> getAccount() throws RequestException {
        if (null == account || newTransactionsExist()) {
            refresh();
        }

        return Optional.ofNullable(account);
    }

    @Override
    public Optional<TradeSummary> getLastClosedTrade() throws RequestException {
        if (null == account || newTransactionsExist()) {
            refresh();
        }

        return lastTenClosedTrades.isEmpty() ? Optional.empty() : Optional.of(lastTenClosedTrades.last());
    }

    // TODO: Convert this to just merge the rest of the changes (e.g. opened positions)
    private boolean newTransactionsExist() throws RequestException {
        TransactionID currentTransactionId = account.getLastTransactionID();

        AccountChangesRequest request = new AccountChangesRequest(account.getId());
        request.setSinceTransactionID(currentTransactionId);

        AccountChangesResponse changes = this.ctx.accountChanges(request);
        TransactionID lastTransactionID = changes.getLastTransactionID();

        boolean changesExist = !lastTransactionID.equals(currentTransactionId);

        if (changesExist) {
            LOG.info("Changes exist: transaction id {} != {}", lastTransactionID, currentTransactionId);

            List<TradeSummary> tradesClosed = changes.getAccountChanges().getTradesClosed();
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

        return changesExist;
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
