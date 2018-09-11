package broker;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static broker.Quote.formatDollars;
import static broker.Quote.pippetesFromDouble;
import static broker.Quote.profitLossDisplay;

public class Account {
    private final AccountID id;
    private final long balance;
    private final long netAssetValue;
    private final TransactionID lastTransactionID;
    private final List<TradeSummary> trades;
    private final long profitLoss;

    public Account(AccountID id, long balance, long netAssetValue, TransactionID lastTransactionID, List<TradeSummary> trades, long profitLoss) {
        this.id = id;
        this.balance = balance;
        this.netAssetValue = netAssetValue;
        this.lastTransactionID = lastTransactionID;
        this.trades = trades;
        this.profitLoss = profitLoss;
    }

    public AccountID getId() {
        return id;
    }

    public long getNetAssetValue() {
        return netAssetValue;
    }

    public long getBalance() {
        return balance;
    }

    public TransactionID getLastTransactionID() {
        return lastTransactionID;
    }

    public List<TradeSummary> getTrades() {
        return trades;
    }

    public long getPl() {
        return profitLoss;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return balance == account.balance &&
                netAssetValue == account.netAssetValue &&
                profitLoss == account.profitLoss &&
                Objects.equals(id, account.id) &&
                Objects.equals(lastTransactionID, account.lastTransactionID) &&
                Objects.equals(trades, account.trades);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, balance, netAssetValue, lastTransactionID, trades, profitLoss);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("balance", formatDollars(balance))
                .add("netAssetValue", formatDollars(netAssetValue))
                .add("lastTransactionID", lastTransactionID)
                .add("trades", trades)
                .add("profitLoss", profitLossDisplay(profitLoss))
                .toString();
    }

    public Account positionOpened(TradeSummary position, TransactionID latestTransactionID) {
        long newBalance = this.balance - position.getPurchaseValue();

        List<TradeSummary> newTrades = new ArrayList<>(this.trades);
        newTrades.add(position);

        return new Account.Builder(this.id)
                .withBalance(newBalance)
                .withNetAssetValue(calculateNav(newBalance, newTrades))
                .withLastTransactionID(latestTransactionID)
                .withTrades(newTrades)
                .withProfitLoss(this.profitLoss)
                .build();
    }

    public Account positionClosed(TradeSummary position, TransactionID latestTransactionID) {
        long newBalance = this.balance + position.getNetAssetValue();
        long newProfitLoss = this.profitLoss + position.getRealizedProfitLoss();

        List<TradeSummary> newTrades = new ArrayList<>(this.trades);
        newTrades.removeIf(it -> it.getId().equals(position.getId()));

        return new Account.Builder(this.id)
                .withBalance(newBalance)
                .withNetAssetValue(calculateNav(newBalance, newTrades))
                .withLastTransactionID(latestTransactionID)
                .withTrades(newTrades)
                .withProfitLoss(newProfitLoss)
                .build();
    }

    public Account incorporateState(AccountChangesState stateChanges) {
        // TODO: Add unrealized P&L to account
        List<TradeSummary> newTrades = TradeSummary.incorporateState(this.trades, stateChanges);
        // This intentionally calculates NAV on its own to make sure our calculations stay in line with the broker
        return new Account(this.id, this.balance, calculateNav(this.balance, newTrades), this.lastTransactionID, newTrades, this.profitLoss);
    }

    public static long calculateNav(long balance, List<TradeSummary> trades) {
        return balance + trades.stream().mapToLong(TradeSummary::getNetAssetValue).sum();
    }

    public Account adjustBalance(long balanceAdjustment) {
        long newBalance = this.balance + balanceAdjustment;
        long newNav = calculateNav(newBalance, trades);

        return new Account(id, newBalance, newNav, lastTransactionID, trades, profitLoss);
    }

    public static class Builder {
        private final AccountID id;
        private long balance = 0L;
        private long netAssetValue = 0L;
        private TransactionID lastTransactionID;
        private List<TradeSummary> trades = Collections.emptyList();
        private long profitLoss = 0L;

        public Builder(AccountID id) {
            this.id = id;
        }

        public Builder withBalanceDollars(int balanceDollars) {
            return withBalance(pippetesFromDouble(balanceDollars));
        }

        public Builder withBalance(long balance) {
            this.balance = balance;
            return this;
        }

        public Builder withNetAssetValue(long netAssetValue) {
            this.netAssetValue = netAssetValue;
            return this;
        }

        public Builder withLastTransactionID(TransactionID lastTransactionID) {
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

            return new Account(id, balance, netAssetValue, lastTransactionID, trades, profitLoss);
        }
    }
}
