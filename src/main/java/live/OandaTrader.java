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

import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

public class OandaTrader extends BaseTrader {

    private static final Logger LOG = LoggerFactory.getLogger(OandaTrader.class);

    private final String accountId;
    private final Context ctx;
    private Account account;
    private SortedSet<TradeSummary> lastTenClosedTrades = new TreeSet<>();

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

    private boolean newTransactionsExist() throws RequestException {
        AccountChangesRequest request = new AccountChangesRequest(account.getId());
        request.setSinceTransactionID(account.getLastTransactionID());

        AccountChangesResponse changes = this.ctx.accountChanges(request);
        TransactionID lastTransactionID = changes.getLastTransactionID();

        return !lastTransactionID.equals(account.getLastTransactionID());
    }

    private void refresh() {
        Stopwatch timer = Stopwatch.createStarted();

        try {
            account = ctx.getAccount(new AccountID(this.accountId)).getAccount();

            TradeListResponse tradeListResponse = ctx.listTrade(new TradeListRequest(new AccountID(this.accountId), 10));
            lastTenClosedTrades.clear();
            lastTenClosedTrades.addAll(tradeListResponse.getTrades());

            LOG.info("Loaded account {} and closed trades in {}", accountId, timer);
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
