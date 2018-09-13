package broker;

import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.Objects;

public class AccountAndTrades {
    private final Account account;
    private final List<Trade> trades;

    public AccountAndTrades(Account account, List<Trade> trades) {
        this.account = account;
        this.trades = trades;
    }

    public Account getAccount() {
        return account;
    }

    public List<Trade> getTrades() {
        return trades;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountAndTrades that = (AccountAndTrades) o;
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
                .add("trades", trades)
                .toString();
    }
}
