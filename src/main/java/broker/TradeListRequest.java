package broker;

public class TradeListRequest {
    private final AccountID accountID;
    private final int count;

    public TradeListRequest(AccountID accountID, int count) {
        this.accountID = accountID;
        this.count = count;
    }

    public AccountID getAccountID() {
        return accountID;
    }

    public int getCount() {
        return count;
    }
}
