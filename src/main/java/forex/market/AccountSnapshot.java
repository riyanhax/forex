package forex.market;

import com.google.common.base.MoreObjects;
import forex.broker.AccountSummary;
import forex.broker.TradeSummary;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AccountSnapshot implements Comparable<AccountSnapshot> {

    private final AccountSummary account;
    private final LocalDateTime timestamp;

    public AccountSnapshot(AccountSummary account, LocalDateTime timestamp) {
        this.account = account;
        this.timestamp = timestamp;
    }

    public AccountSummary getAccount() {
        return account;
    }

    public long getPipettesProfit() {
        return account.getProfitLoss();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public long getNetAssetValue() {
        return account.getNetAssetValue();
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
