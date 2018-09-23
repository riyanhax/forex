package forex.broker;

public class TradeListRequest {
    private final String accountID;
    private final TradeStateFilter filter;
    private final int count;

    public TradeListRequest(String accountID, TradeStateFilter filter, int count) {
        this.accountID = accountID;
        this.filter = filter;
        this.count = count;
    }

    public String getAccountID() {
        return accountID;
    }

    public TradeStateFilter getFilter() {
        return filter;
    }

    public int getCount() {
        return count;
    }
}
