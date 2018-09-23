package forex.broker;

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static forex.broker.Quote.formatDollars;
import static forex.broker.Quote.pippetesFromDouble;
import static forex.broker.Quote.profitLossDisplay;

public class Account {
    private static final Logger LOG = LoggerFactory.getLogger(Account.class);

    private final String id;
    private final long balance;
    private final String lastTransactionID;
    private final List<TradeSummary> trades;
    private final long profitLoss;

    public Account(String id, long balance, String lastTransactionID, List<TradeSummary> trades, long profitLoss) {
        this.id = id;
        this.balance = balance;
        this.lastTransactionID = lastTransactionID;
        this.trades = trades;
        this.profitLoss = profitLoss;
    }

    public String getId() {
        return id;
    }

    public long getNetAssetValue() {
        return balance + trades.stream().mapToLong(TradeSummary::getNetAssetValue).sum();
    }

    public long getBalance() {
        return balance;
    }

    public String getLastTransactionID() {
        return lastTransactionID;
    }

    public List<TradeSummary> getTrades() {
        return trades;
    }

    public long getProfitLoss() {
        return profitLoss;
    }

    public long getUnrealizedProfitLoss() {
        return trades.stream().mapToLong(TradeSummary::getUnrealizedProfitLoss).sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return balance == account.balance &&
                profitLoss == account.profitLoss &&
                Objects.equals(id, account.id) &&
                Objects.equals(lastTransactionID, account.lastTransactionID) &&
                Objects.equals(trades, account.trades);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, balance, lastTransactionID, trades, profitLoss);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("balance", formatDollars(balance))
                .add("netAssetValue", formatDollars(getNetAssetValue()))
                .add("lastTransactionID", lastTransactionID)
                .add("trades", trades)
                .add("profitLoss", profitLossDisplay(profitLoss))
                .toString();
    }

    public Account positionOpened(TradeSummary position, String latestTransactionID) {
        long newBalance = this.balance - position.getPurchaseValue();

        List<TradeSummary> newTrades = new ArrayList<>(this.trades);
        newTrades.add(position);

        return new Account.Builder(this.id)
                .withBalance(newBalance)
                .withLastTransactionID(latestTransactionID)
                .withTrades(newTrades)
                .withProfitLoss(this.profitLoss)
                .build();
    }

    public Account positionClosed(TradeSummary position, String latestTransactionID) {
        long newBalance = this.balance + position.getNetAssetValue();
        long newProfitLoss = this.profitLoss + position.getRealizedProfitLoss();

        List<TradeSummary> newTrades = new ArrayList<>(this.trades);
        newTrades.removeIf(it -> it.getId().equals(position.getId()));

        return new Account.Builder(this.id)
                .withBalance(newBalance)
                .withLastTransactionID(latestTransactionID)
                .withTrades(newTrades)
                .withProfitLoss(newProfitLoss)
                .build();
    }

    Account processStateChanges(AccountChangesState stateChanges) {
        List<TradeSummary> newTrades = TradeSummary.incorporateState(this.trades, stateChanges);
        long newBalance = this.balance;

        Account newAccount = new Account(this.id, newBalance, this.lastTransactionID, newTrades, this.profitLoss);

        // This intentionally calculates NAV on its own to make sure our calculations stay in line with the broker
        long newNAV = newAccount.getNetAssetValue();

        // Would need to consider financing charges  and probably interest for the NAV to match exactly.
        // So this is adjusting the balance when some kind of discrepancy exists.
        long brokerNetAssetValue = stateChanges.getNetAssetValue();
        long balanceAdjustment = brokerNetAssetValue == newNAV ? 0L :
                brokerNetAssetValue - newNAV;

        if (balanceAdjustment != 0) {
            newBalance = this.balance + balanceAdjustment;
            newAccount = new Account(this.id, newBalance, this.lastTransactionID, newTrades, this.profitLoss);
        }

        long newUnrealizedProfitLoss = newAccount.getUnrealizedProfitLoss();

        if (newUnrealizedProfitLoss != stateChanges.getUnrealizedProfitAndLoss()) {
            LOG.error("Why wasn't the broker's unrealized profit and loss the same as ours? Open trades: {}", newTrades);
        }

        LOG.info("Broker NAV: {}, Calculated NAV: {}{}, Broker unrealized profit: {}, Calculated unrealized profit: {}",
                formatDollars(brokerNetAssetValue),
                formatDollars(newNAV),
                balanceAdjustment == 0 ? "" : String.format(" [adjusted %s]", formatDollars(balanceAdjustment)),
                formatDollars(stateChanges.getUnrealizedProfitAndLoss()),
                formatDollars(newUnrealizedProfitLoss));

        return newAccount;
    }

    public Account processChanges(AccountChangesResponse state) {
        String mostRecentTransactionID = state.getLastTransactionID();

        boolean changesExist = !mostRecentTransactionID.equals(lastTransactionID);

        Account newAccount = this;

        if (changesExist) {
            LOG.info("Changes exist: transaction id {} != {}", mostRecentTransactionID, lastTransactionID);

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

    public static class Builder {
        private final String id;
        private long balance = 0L;
        private String lastTransactionID;
        private List<TradeSummary> trades = Collections.emptyList();
        private long profitLoss = 0L;

        public Builder(String id) {
            this.id = id;
        }

        public Builder withBalanceDollars(int balanceDollars) {
            return withBalance(pippetesFromDouble(balanceDollars));
        }

        public Builder withBalance(long balance) {
            this.balance = balance;
            return this;
        }

        public Builder withLastTransactionID(String lastTransactionID) {
            this.lastTransactionID = lastTransactionID;
            return this;
        }

        public Builder withTrades(List<TradeSummary> trades) {
            this.trades = trades;
            return this;
        }

        public Builder withProfitLoss(long profitLoss) {
            this.profitLoss = profitLoss;
            return this;
        }

        public Account build() {
            Objects.requireNonNull(id);
            Objects.requireNonNull(trades);

            return new Account(id, balance, lastTransactionID, trades, profitLoss);
        }

    }
}
