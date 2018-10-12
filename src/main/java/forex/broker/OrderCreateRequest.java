package forex.broker;

import forex.market.Instrument;

public class OrderCreateRequest {

    private final String accountID;
    private MarketOrderRequest order;
    private LimitOrderRequest limitOrder;

    public OrderCreateRequest(String accountID) {
        this.accountID = accountID;
    }

    public OrderCreateRequest setOrder(MarketOrderRequest order) {
        this.order = order;
        return this;
    }

    public OrderCreateRequest setOrder(LimitOrderRequest order) {
        this.limitOrder = order;
        return this;
    }

    public String getAccountID() {
        return accountID;
    }

    public MarketOrderRequest getOrder() {
        return order;
    }

    public LimitOrderRequest getLimitOrder() {
        return limitOrder;
    }

    public Instrument getInstrument() {
        return order == null ? limitOrder.getInstrument() : order.getInstrument();
    }
}
