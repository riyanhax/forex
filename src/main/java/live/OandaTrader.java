package live;

import broker.Account;
import broker.AccountChangesRequest;
import broker.AccountChangesResponse;
import broker.AccountID;
import broker.Context;
import broker.RequestException;
import broker.TransactionID;
import com.google.common.base.Stopwatch;
import market.InstrumentHistoryService;
import market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import trader.BaseTrader;
import trader.TradingStrategy;

public class OandaTrader extends BaseTrader {

    private static final Logger LOG = LoggerFactory.getLogger(OandaTrader.class);

    private final String accountId;
    private final Context ctx;
    private Account account;

    public OandaTrader(String accountId, Context ctx, TradingStrategy tradingStrategy, MarketTime clock,
                InstrumentHistoryService instrumentHistoryService) throws Exception {
        super(tradingStrategy, clock, instrumentHistoryService);
        this.accountId = accountId;
        this.ctx = ctx;
        this.account = refresh();
    }

    @Override
    public String getAccountNumber() {
        return accountId;
    }

    Context getContext() {
        return ctx;
    }

    Account getAccount() throws Exception {
        if (newTransactionsExist()) {
            account = refresh();
        }

        return account;
    }

    private boolean newTransactionsExist() throws RequestException {
        AccountChangesRequest request = new AccountChangesRequest(account.getId());
        request.setSinceTransactionID(account.getLastTransactionID());

        AccountChangesResponse changes = this.ctx.account().changes(request);
        TransactionID lastTransactionID = changes.getLastTransactionID();

        return !lastTransactionID.equals(account.getLastTransactionID());
    }

    private Account refresh() throws Exception {
        Stopwatch timer = Stopwatch.createStarted();

        Account account = ctx.account().get(new AccountID(this.accountId)).getAccount();

        LOG.info("Loaded account {} in {}", accountId, timer);

        return account;
    }
}
