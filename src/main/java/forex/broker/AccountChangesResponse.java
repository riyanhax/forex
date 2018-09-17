package forex.broker;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class AccountChangesResponse {
    private final TransactionID lastTransactionID;
    private final AccountChanges accountChanges;
    private final AccountChangesState accountChangesState;

    public AccountChangesResponse(TransactionID lastTransactionID,
                                  AccountChanges accountChanges,
                                  AccountChangesState accountChangesState) {
        this.lastTransactionID = lastTransactionID;
        this.accountChanges = accountChanges;
        this.accountChangesState = accountChangesState;
    }

    public TransactionID getLastTransactionID() {
        return lastTransactionID;
    }

    public AccountChanges getAccountChanges() {
        return accountChanges;
    }

    public AccountChangesState getAccountChangesState() {
        return accountChangesState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountChangesResponse that = (AccountChangesResponse) o;
        return Objects.equals(lastTransactionID, that.lastTransactionID) &&
                Objects.equals(accountChanges, that.accountChanges) &&
                Objects.equals(accountChangesState, that.accountChangesState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastTransactionID, accountChanges, accountChangesState);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("lastTransactionID", lastTransactionID)
                .add("accountChanges", accountChanges)
                .add("accountChangesState", accountChangesState)
                .toString();
    }

    public AccountChangesResponse tradeOpened(Integer transactionId, TradeSummary filledPosition, AccountChangesState state) {
        AccountChanges changes = accountChanges.tradeOpened(filledPosition);

        return new AccountChangesResponse(new TransactionID(transactionId.toString()), changes, state);
    }

    public AccountChangesResponse tradeClosed(Integer transactionId, TradeSummary filledPosition, AccountChangesState state) {
        AccountChanges changes = accountChanges.tradeClosed(filledPosition);

        return new AccountChangesResponse(new TransactionID(transactionId.toString()), changes, state);
    }

    public static AccountChangesResponse empty(TransactionID latestTransactionId, AccountChangesState accountChangesState) {
        return new AccountChangesResponse(latestTransactionId, AccountChanges.empty(), accountChangesState);
    }
}
