package forex.broker;

import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;
import java.util.Objects;

public class OrderCancelTransaction {

    private final String orderID;
    private final OrderCancelReason reason;
    private final String id;
    private final String requestID;
    private final LocalDateTime time;

    public OrderCancelTransaction(String orderID, OrderCancelReason reason, String id, String requestID, LocalDateTime time) {
        this.orderID = orderID;
        this.reason = reason;
        this.id = id;
        this.requestID = requestID;
        this.time = time;
    }

    public String getOrderID() {
        return orderID;
    }

    public OrderCancelReason getReason() {
        return reason;
    }

    public String getId() {
        return id;
    }

    public String getRequestID() {
        return requestID;
    }

    public LocalDateTime getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderCancelTransaction that = (OrderCancelTransaction) o;
        return Objects.equals(orderID, that.orderID) &&
                reason == that.reason &&
                Objects.equals(id, that.id) &&
                Objects.equals(requestID, that.requestID) &&
                Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderID, reason, id, requestID, time);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("orderID", orderID)
                .add("reason", reason)
                .add("id", id)
                .add("requestID", requestID)
                .add("time", time)
                .toString();
    }
}
