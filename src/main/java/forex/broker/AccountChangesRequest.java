package forex.broker;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class AccountChangesRequest {
    private final String accountID;
    private final TransactionID sinceTransactionID;

    public AccountChangesRequest(String accountID, TransactionID sinceTransactionID) {
        this.accountID = accountID;
        this.sinceTransactionID = sinceTransactionID;
    }

    public String getAccountID() {
        return accountID;
    }

    public TransactionID getSinceTransactionID() {
        return sinceTransactionID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountChangesRequest that = (AccountChangesRequest) o;
        return Objects.equals(accountID, that.accountID) &&
                Objects.equals(sinceTransactionID, that.sinceTransactionID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountID, sinceTransactionID);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("accountID", accountID)
                .add("sinceTransactionID", sinceTransactionID)
                .toString();
    }
}
