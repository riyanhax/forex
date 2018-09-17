package forex.broker;

public class OrderCreateRequest {

    private final AccountID accountID;
    private MarketOrderRequest order;

    public OrderCreateRequest(AccountID accountID) {
        this.accountID = accountID;
    }

    public OrderCreateRequest setOrder(MarketOrderRequest order) {
        this.order = order;
        return this;
    }

    public AccountID getAccountID() {
        return accountID;
    }

    public MarketOrderRequest getOrder() {
        return order;
    }
}
