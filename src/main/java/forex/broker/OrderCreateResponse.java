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
    private final OrderFillTransaction orderFillTransaction;
    private final OrderCancelTransaction orderCancelTransaction;

    public OrderCreateResponse(Instrument instrument, MarketOrderTransaction orderCreateTransaction,
                               OrderFillTransaction orderFillTransaction,
                               OrderCancelTransaction orderCancelTransaction) {
        this.instrument = instrument;
        this.orderCreateTransaction = orderCreateTransaction;
        this.orderFillTransaction = orderFillTransaction;
        this.orderCancelTransaction = orderCancelTransaction;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public MarketOrderTransaction getOrderCreateTransaction() {
        return orderCreateTransaction;
    }

    public OrderFillTransaction getOrderFillTransaction() {
        return orderFillTransaction;
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
                Objects.equals(orderFillTransaction, that.orderFillTransaction) &&
                Objects.equals(orderCancelTransaction, that.orderCancelTransaction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrument, orderCreateTransaction, orderFillTransaction, orderCancelTransaction);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("instrument", instrument)
                .add("orderCreateTransaction", orderCreateTransaction)
                .add("orderFillTransaction", orderFillTransaction)
                .add("orderCancelTransaction", orderCancelTransaction)
                .toString();
    }
}
