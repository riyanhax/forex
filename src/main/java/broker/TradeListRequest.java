package broker;

public class TradeListRequest {
    private final AccountID accountID;
    private final TradeStateFilter filter;
    private final int count;

    public TradeListRequest(AccountID accountID, TradeStateFilter filter, int count) {
        this.accountID = accountID;
        this.filter = filter;
        this.count = count;
    }

    public AccountID getAccountID() {
        return accountID;
    }

    public TradeStateFilter getFilter() {
        return filter;
    }

    public int getCount() {
        return count;
    }
}
