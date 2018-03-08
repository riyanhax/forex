package market;

import market.order.OrderRequest;

public interface OrderListener {
    void orderCancelled(OrderRequest filled);

    void orderFilled(OrderRequest filled);
}
