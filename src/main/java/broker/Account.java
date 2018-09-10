package broker;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Account {
    private final AccountID id;
    private final long balance;
    private final TransactionID lastTransactionID;
    private final List<TradeSummary> trades;
    private long profitLoss;

    public Account(AccountID id, long balance, TransactionID lastTransactionID, List<TradeSummary> trades, long profitLoss) {
        this.id = id;
        this.balance = balance;
        this.lastTransactionID = lastTransactionID;
        this.trades = trades;
        this.profitLoss = profitLoss;
    }

    public AccountID getId() {
        return id;
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
                .add("balance", balance)
                .add("lastTransactionID", lastTransactionID)
                .add("trades", trades)
                .add("profitLoss", profitLoss)
                .toString();
    }

    public Account positionOpened(TradeSummary position, TransactionID latestTransactionID) {
        long newBalance = this.balance - (position.getCurrentUnits() * position.getPrice());

        List<TradeSummary> newTrades = new ArrayList<>(this.trades);
        newTrades.add(position);

        return new Account(this.id, newBalance, latestTransactionID, newTrades, this.profitLoss);
    }

    public Account positionClosed(TradeSummary position, TransactionID latestTransactionID) {
        long newBalance = this.balance + (position.getCurrentUnits() * position.getPrice());
        long newProfitLoss = this.profitLoss + position.getRealizedProfitLoss();

        List<TradeSummary> newTrades = new ArrayList<>(this.trades);
        newTrades.removeIf(it -> it.getId().equals(position.getId()));

        return new Account(this.id, newBalance, latestTransactionID, newTrades, newProfitLoss);
    }
}
