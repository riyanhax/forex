package forex.broker;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.time.LocalDateTime;
import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;

@Entity(name = "account_order")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Order {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "canceled_time")
    private LocalDateTime canceledTime;

    @Column(name = "filled_time")
    private LocalDateTime filledTime;

    @Column
    private OrderCancelReason canceledReason;

    Order() {
    }

    Order(String orderId, String accountId, LocalDateTime createTime, LocalDateTime canceledTime, LocalDateTime filledTime) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.createTime = createTime;
        this.canceledTime = canceledTime;
        this.filledTime = filledTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getCanceledTime() {
        return canceledTime;
    }

    public void setCanceledTime(LocalDateTime canceledTime) {
        this.canceledTime = canceledTime;
    }

    public LocalDateTime getFilledTime() {
        return filledTime;
    }

    public void setFilledTime(LocalDateTime filledTime) {
        this.filledTime = filledTime;
    }

    public OrderCancelReason getCanceledReason() {
        return canceledReason;
    }

    public void setCanceledReason(OrderCancelReason canceledReason) {
        this.canceledReason = canceledReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId) &&
                Objects.equals(accountId, order.accountId) &&
                Objects.equals(createTime, order.createTime) &&
                Objects.equals(canceledTime, order.canceledTime) &&
                Objects.equals(filledTime, order.filledTime) &&
                canceledReason == order.canceledReason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, accountId, createTime, canceledTime, filledTime, canceledReason);
    }

    @Override
    public final String toString() {
        return toStringHelper().toString();
    }

    protected ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this)
                .add("orderId", orderId)
                .add("accountId", accountId)
                .add("createTime", createTime)
                .add("canceledTime", canceledTime)
                .add("filledTime", filledTime)
                .add("canceledReason", canceledReason);
    }
}
