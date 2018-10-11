package forex.market;

import forex.broker.MarketOrder;
import forex.broker.Order;
import forex.broker.Orders;

public interface AccountOrderService {
    /**
     * Saves the order if an order with the same {@link Order#orderId} and {@link Order#accountId}
     * does NOT exist already.
     */
    MarketOrder saveIfNotExists(MarketOrder order);

    /**
     * Saves the order, overwriting any existing order in the database with the same
     * {@link Order#orderId} and {@link Order#accountId}.
     */
    MarketOrder upsert(MarketOrder order);

    MarketOrder findMarketOrder(String orderId, String accountID);

    /**
     * Saves the orders without overwriting existing.
     */
    Orders saveIfNotExists(Orders orders);

    /**
     * Saves the orders and overwriting any existing.
     */
    Orders upsert(Orders orders);
}
