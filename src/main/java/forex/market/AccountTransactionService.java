package forex.market;

import forex.broker.LimitOrderTransaction;
import forex.broker.MarketOrderTransaction;
import forex.broker.OrderCancelTransaction;
import forex.broker.OrderFillTransaction;
import forex.broker.Transaction;

public interface AccountTransactionService {
    /**
     * Saves the transaction if a transaction with the same {@link Transaction#transactionId} and {@link Transaction#accountId}
     * does NOT exist already.
     */
    MarketOrderTransaction saveIfNotExists(MarketOrderTransaction order);

    /**
     * Saves the transaction if a transaction with the same {@link Transaction#transactionId} and {@link Transaction#accountId}
     * does NOT exist already.
     */
    LimitOrderTransaction saveIfNotExists(LimitOrderTransaction order);

    /**
     * Saves the transaction if a transaction with the same {@link Transaction#transactionId} and {@link Transaction#accountId}
     * does NOT exist already.
     */
    OrderFillTransaction saveIfNotExists(OrderFillTransaction order);

    /**
     * Saves the transaction if a transaction with the same {@link Transaction#transactionId} and {@link Transaction#accountId}
     * does NOT exist already.
     */
    OrderCancelTransaction saveIfNotExists(OrderCancelTransaction order);
}
