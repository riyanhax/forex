package forex.broker;

import com.google.common.base.MoreObjects.ToStringHelper;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "account_transaction_order_cancel")
public class OrderCancelTransaction extends Transaction {

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(nullable = false)
    private OrderCancelReason reason;

    @Column(name = "request_id", nullable = false)
    private String requestId;

    public OrderCancelTransaction(String id, String accountId, LocalDateTime time, String orderID, OrderCancelReason reason, String requestId) {
        super(id, accountId, time);

        this.orderId = orderID;
        this.reason = reason;
        this.requestId = requestId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public OrderCancelReason getReason() {
        return reason;
    }

    public void setReason(OrderCancelReason reason) {
        this.reason = reason;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OrderCancelTransaction that = (OrderCancelTransaction) o;
        return Objects.equals(orderId, that.orderId) &&
                reason == that.reason &&
                Objects.equals(requestId, that.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), orderId, reason, requestId);
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("orderId", orderId)
                .add("reason", reason)
                .add("requestId", requestId);
    }
}
