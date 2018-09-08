package broker;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class TradeCloseResponse {

    /**
     * The MarketOrder Transaction created to close the Trade.
     */
    private final MarketOrderTransaction orderCreateTransaction;

    public TradeCloseResponse(MarketOrderTransaction orderCreateTransaction) {
        this.orderCreateTransaction = orderCreateTransaction;
    }

    public MarketOrderTransaction getOrderCreateTransaction() {
        return orderCreateTransaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeCloseResponse that = (TradeCloseResponse) o;
        return Objects.equals(orderCreateTransaction, that.orderCreateTransaction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderCreateTransaction);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("orderCreateTransaction", orderCreateTransaction)
                .toString();
    }
}
