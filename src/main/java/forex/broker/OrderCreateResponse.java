package forex.broker;

import com.google.common.base.MoreObjects;
import forex.market.Instrument;

import java.util.Objects;

public class OrderCreateResponse {

    private final Instrument instrument;

    /**
     * The Transaction that created the Order specified by the request.
     */
    private final MarketOrderTransaction orderCreateTransaction;
    private final OrderCancelTransaction orderCancelTransaction;

    public OrderCreateResponse(Instrument instrument, MarketOrderTransaction orderCreateTransaction,
                               OrderCancelTransaction orderCancelTransaction) {
        this.instrument = instrument;
        this.orderCreateTransaction = orderCreateTransaction;
        this.orderCancelTransaction = orderCancelTransaction;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public MarketOrderTransaction getOrderCreateTransaction() {
        return orderCreateTransaction;
    }

    public OrderCancelTransaction getOrderCancelTransaction() {
        return orderCancelTransaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderCreateResponse that = (OrderCreateResponse) o;
        return instrument == that.instrument &&
                Objects.equals(orderCreateTransaction, that.orderCreateTransaction) &&
                Objects.equals(orderCancelTransaction, that.orderCancelTransaction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrument, orderCreateTransaction, orderCancelTransaction);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("instrument", instrument)
                .add("orderCreateTransaction", orderCreateTransaction)
                .add("orderCancelTransaction", orderCancelTransaction)
                .toString();
    }
}
