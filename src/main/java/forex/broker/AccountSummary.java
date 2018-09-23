package forex.broker;

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static forex.broker.Quote.formatDollars;

public class AccountSummary {
    private static final Logger LOG = LoggerFactory.getLogger(AccountSummary.class);

    private final Account account;
    private final List<TradeSummary> trades;

    public AccountSummary(Account account, List<TradeSummary> trades) {
        this.account = account;
        this.trades = trades;
    }

    public Account getAccount() {
        return account;
    }

    public long getNetAssetValue() {
        return account.getBalance() + trades.stream().mapToLong(TradeSummary::getNetAssetValue).sum();
    }

    public String getId() {
        return account.getId();
    }

    public long getBalance() {
        return account.getBalance();
    }

    public String getLastTransactionID() {
        return account.getLastTransactionID();
    }

    public List<TradeSummary> getTrades() {
        return trades;
    }

    public long getProfitLoss() {
        return account.getProfitLoss();
    }

    public long getUnrealizedProfitLoss() {
        return trades.stream().mapToLong(TradeSummary::getUnrealizedProfitLoss).sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountSummary that = (AccountSummary) o;
        return Objects.equals(account, that.account) &&
                Objects.equals(trades, that.trades);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, trades);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("account", account)
                .add("netAssetValue", formatDollars(getNetAssetValue()))
                .add("trades", trades)
                .toString();
    }

    public AccountSummary positionOpened(TradeSummary position, String latestTransactionID) {
        Account newAccount = account.positionOpened(position, latestTransactionID);

        List<TradeSummary> newTrades = new ArrayList<>(this.trades);
        newTrades.add(position);

        return new AccountSummary(newAccount, newTrades);
    }

    public AccountSummary positionClosed(TradeSummary position, String latestTransactionID) {
        Account newAccount = account.positionClosed(position, latestTransactionID);

        List<TradeSummary> newTrades = new ArrayList<>(this.trades);
        newTrades.removeIf(it -> it.getTradeId().equals(position.getTradeId()));

        return new AccountSummary(newAccount, newTrades);
    }

    AccountSummary processStateChanges(AccountChangesState stateChanges) {
        List<TradeSummary> newTrades = TradeSummary.incorporateState(this.trades, stateChanges);
        AccountSummary newSummary = new AccountSummary(this.account, newTrades);

        // This intentionally calculates NAV on its own to make sure our calculations stay in line with the broker
        long newNAV = newSummary.getNetAssetValue();

        // Would need to consider financing charges  and probably interest for the NAV to match exactly.
        // So this is adjusting the balance when some kind of discrepancy exists.
        long brokerNetAssetValue = stateChanges.getNetAssetValue();
        long balanceAdjustment = brokerNetAssetValue == newNAV ? 0L :
                brokerNetAssetValue - newNAV;

        if (balanceAdjustment != 0) {
            long newBalance = account.getBalance() + balanceAdjustment;
            Account newAccount = new Account(this.account.getId(), newBalance, this.account.getLastTransactionID(), this.account.getProfitLoss());
            newSummary = new AccountSummary(newAccount, newTrades);
        }

        long newUnrealizedProfitLoss = newSummary.getUnrealizedProfitLoss();

        if (newUnrealizedProfitLoss != stateChanges.getUnrealizedProfitAndLoss()) {
            LOG.error("Why wasn't the broker's unrealized profit and loss the same as ours? Open trades: {}", newTrades);
        }

        LOG.info("Broker NAV: {}, Calculated NAV: {}{}, Broker unrealized profit: {}, Calculated unrealized profit: {}",
                formatDollars(brokerNetAssetValue),
                formatDollars(newNAV),
                balanceAdjustment == 0 ? "" : String.format(" [adjusted %s]", formatDollars(balanceAdjustment)),
                formatDollars(stateChanges.getUnrealizedProfitAndLoss()),
                formatDollars(newUnrealizedProfitLoss));

        return newSummary;
    }

    public AccountSummary processChanges(AccountChangesResponse state) {
        String mostRecentTransactionID = state.getLastTransactionID();

        boolean changesExist = !mostRecentTransactionID.equals(getLastTransactionID());

        AccountSummary newAccount = this;

        if (changesExist) {
            LOG.info("Changes exist: transaction id {} != {}", mostRecentTransactionID, getLastTransactionID());

            AccountChanges changes = state.getAccountChanges();
            List<TradeSummary> tradesClosed = changes.getTradesClosed();
            List<TradeSummary> tradesOpened = changes.getTradesOpened();

            if (!tradesClosed.isEmpty()) {
                for (TradeSummary closedTrade : tradesClosed) {
                    newAccount = newAccount.positionClosed(closedTrade, mostRecentTransactionID);
                }
            }

            for (TradeSummary openedTrade : tradesOpened) {
                newAccount = newAccount.positionOpened(openedTrade, mostRecentTransactionID);
            }
        }

        AccountChangesState stateChanges = state.getAccountChangesState();

        return newAccount.processStateChanges(stateChanges);
    }
}
