package forex.broker;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class AccountGetResponse {
    private AccountSummary account;

    public AccountGetResponse(AccountSummary account) {
        this.account = account;
    }

    public AccountSummary getAccount() {
        return account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountGetResponse that = (AccountGetResponse) o;
        return Objects.equals(account, that.account);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("account", account)
                .toString();
    }
}
