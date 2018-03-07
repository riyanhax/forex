package broker;

public class AccountChangesRequest {
    private final AccountID accountID;
    private TransactionID sinceTransactionID;

    public AccountChangesRequest(AccountID accountID) {
        this.accountID = accountID;
    }

    public AccountID getAccountID() {
        return accountID;
    }

    public void setSinceTransactionID(TransactionID sinceTransactionID) {
        this.sinceTransactionID = sinceTransactionID;
    }

    public TransactionID getSinceTransactionID() {
        return sinceTransactionID;
    }
}
