package broker;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class AccountChangesResponse {
    private final TransactionID lastTransactionID;
    private final AccountChanges accountChanges;

    public AccountChangesResponse(TransactionID lastTransactionID, AccountChanges accountChanges) {
        this.lastTransactionID = lastTransactionID;
        this.accountChanges = accountChanges;
    }

    public TransactionID getLastTransactionID() {
        return lastTransactionID;
    }

    public AccountChanges getAccountChanges() {
        return accountChanges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountChangesResponse that = (AccountChangesResponse) o;
        return Objects.equals(lastTransactionID, that.lastTransactionID) &&
                Objects.equals(accountChanges, that.accountChanges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastTransactionID, accountChanges);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("lastTransactionID", lastTransactionID)
                .add("accountChanges", accountChanges)
                .toString();
    }

    public AccountChangesResponse tradeOpened(Integer transactionId, TradeSummary filledPosition) {
        AccountChanges changes = accountChanges.tradeOpened(filledPosition);

        return new AccountChangesResponse(new TransactionID(transactionId.toString()), changes);
    }

    public AccountChangesResponse tradeClosed(Integer transactionId, TradeSummary filledPosition) {
        AccountChanges changes = accountChanges.tradeClosed(filledPosition);

        return new AccountChangesResponse(new TransactionID(transactionId.toString()), changes);
    }

    public static AccountChangesResponse empty(TransactionID latestTransactionId) {
        return new AccountChangesResponse(latestTransactionId, AccountChanges.empty());
    }
}
