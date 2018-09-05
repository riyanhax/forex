package live;

import broker.Account;
import broker.AccountChangesRequest;
import broker.AccountChangesResponse;
import broker.AccountID;
import broker.Context;
import broker.RequestException;
import broker.TransactionID;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Stopwatch;
import market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import trader.BaseTrader;
import trader.TradingStrategy;

import java.util.Optional;

public class OandaTrader extends BaseTrader {

    private static final Logger LOG = LoggerFactory.getLogger(OandaTrader.class);

    private final String accountId;
    private final Context ctx;
    private Account account;

    public OandaTrader(String accountId, Context ctx, TradingStrategy tradingStrategy, MarketTime clock) throws Exception {
        super(tradingStrategy, clock);
        this.accountId = accountId;
        this.ctx = ctx;
        this.account = refresh().orElse(null);
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
            refresh().ifPresent(it -> account = it);
        }

        return Optional.ofNullable(account);
    }

    private boolean newTransactionsExist() throws RequestException {
        AccountChangesRequest request = new AccountChangesRequest(account.getId());
        request.setSinceTransactionID(account.getLastTransactionID());

        AccountChangesResponse changes = this.ctx.accountChanges(request);
        TransactionID lastTransactionID = changes.getLastTransactionID();

        return !lastTransactionID.equals(account.getLastTransactionID());
    }

    private Optional<Account> refresh() {
        Stopwatch timer = Stopwatch.createStarted();

        try {
            Account account = ctx.getAccount(new AccountID(this.accountId)).getAccount();

            LOG.info("Loaded account {} in {}", accountId, timer);

            return Optional.of(account);
        } catch (RequestException e) {
            LOG.error("Unable to retrieve the account!", e);
            return Optional.empty();
        }
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("accountId", accountId);
    }
}
