package broker;

public class AccountChangesResponse {
    private TransactionID lastTransactionID;

    public AccountChangesResponse(TransactionID lastTransactionID) {
        this.lastTransactionID = lastTransactionID;
    }

    public TransactionID getLastTransactionID() {
        return lastTransactionID;
    }
}
