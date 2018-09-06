package broker;

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
}
