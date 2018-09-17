package forex.market;

import forex.market.order.OrderRequest;

public interface OrderListener {
    void orderCancelled(OrderRequest filled);

    void orderFilled(OrderRequest filled);
}
