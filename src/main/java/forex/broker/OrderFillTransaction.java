package forex.broker;

import com.google.common.base.MoreObjects.ToStringHelper;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "account_transaction_order_fill")
public class OrderFillTransaction extends Transaction {

    @Column(name = "order_id", nullable = false)
    private String orderId;

    public OrderFillTransaction() {
    }

    public OrderFillTransaction(String transactionId, String accountId, LocalDateTime time, String orderId) {
        super(transactionId, accountId, time);

        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OrderFillTransaction that = (OrderFillTransaction) o;
        return Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), orderId);
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("orderId", orderId);
    }
}
