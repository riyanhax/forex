package forex.broker;

public class OrderCreateRequest {

    private final String accountID;
    private MarketOrderRequest order;

    public OrderCreateRequest(String accountID) {
        this.accountID = accountID;
    }

    public OrderCreateRequest setOrder(MarketOrderRequest order) {
        this.order = order;
        return this;
    }

    public String getAccountID() {
        return accountID;
    }

    public MarketOrderRequest getOrder() {
        return order;
    }
}
