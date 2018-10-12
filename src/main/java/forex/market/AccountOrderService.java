package forex.market;

import forex.broker.LimitOrder;
import forex.broker.MarketOrder;
import forex.broker.Order;
import forex.broker.Orders;
import forex.broker.StopLossOrder;
import forex.broker.TakeProfitOrder;

public interface AccountOrderService {
    /**
     * Saves the order if an order with the same {@link Order#orderId} and {@link Order#accountId}
     * does NOT exist already.
     */
    MarketOrder saveIfNotExists(MarketOrder order);

    /**
     * Saves the order if an order with the same {@link Order#orderId} and {@link Order#accountId}
     * does NOT exist already.
     */
    LimitOrder saveIfNotExists(LimitOrder order);

    /**
     * Saves the order if an order with the same {@link Order#orderId} and {@link Order#accountId}
     * does NOT exist already.
     */
    StopLossOrder saveIfNotExists(StopLossOrder order);

    /**
     * Saves the order if an order with the same {@link Order#orderId} and {@link Order#accountId}
     * does NOT exist already.
     */
    TakeProfitOrder saveIfNotExists(TakeProfitOrder order);

    /**
     * Saves the order, overwriting any existing order in the database with the same
     * {@link Order#orderId} and {@link Order#accountId}.
     */
    MarketOrder upsert(MarketOrder order);

    /**
     * Saves the order, overwriting any existing order in the database with the same
     * {@link Order#orderId} and {@link Order#accountId}.
     */
    LimitOrder upsert(LimitOrder order);

    /**
     * Saves the order, overwriting any existing order in the database with the same
     * {@link Order#orderId} and {@link Order#accountId}.
     */
    StopLossOrder upsert(StopLossOrder order);

    /**
     * Saves the order, overwriting any existing order in the database with the same
     * {@link Order#orderId} and {@link Order#accountId}.
     */
    TakeProfitOrder upsert(TakeProfitOrder order);

    MarketOrder findMarketOrder(String orderId, String accountID);

    LimitOrder findLimitOrder(String orderId, String accountID);

    StopLossOrder findStopLossOrder(String orderId, String accountID);

    TakeProfitOrder findTakeProfitOrder(String orderId, String accountID);

    /**
     * Saves the orders without overwriting existing.
     */
    Orders saveIfNotExists(Orders orders);

    /**
     * Saves the orders and overwriting any existing.
     */
    Orders upsert(Orders orders);

    Orders findPendingOrders(String accountId);
}
