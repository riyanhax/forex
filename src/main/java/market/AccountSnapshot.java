package market;

import broker.Account;
import broker.TradeSummary;
import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AccountSnapshot implements Comparable<AccountSnapshot> {

    private final Account account;
    private final LocalDateTime timestamp;

    public AccountSnapshot(Account account, LocalDateTime timestamp) {
        this.account = account;
        this.timestamp = timestamp;
    }

    public Account getAccount() {
        return account;
    }

    public long getPipettesProfit() {
        return account.getPl();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public long netAssetValue() {
        // TODO: Write test that this calculates the correct value based on open trades
        return account.getBalance() + account.getTrades().stream()
                .mapToLong(it -> it.getPrice() *it.getCurrentUnits()).sum();
    }

    public long unrealizedProfitAndLoss() {
        return account.getTrades().stream()
                .mapToLong(TradeSummary::getUnrealizedProfitLoss)
                .sum();
    }

    public long pipettes() {
        return getPipettesProfit() + unrealizedProfitAndLoss();
    }

    public List<TradeSummary> getPositionValues() {
        return account.getTrades();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountSnapshot that = (AccountSnapshot) o;
        return Objects.equals(account, that.account) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, timestamp);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("account", account)
                .add("timestamp", timestamp)
                .toString();
    }

    @Override
    public int compareTo(AccountSnapshot o) {
        return Comparator.comparing(AccountSnapshot::getTimestamp).compare(this, o);
    }
}
