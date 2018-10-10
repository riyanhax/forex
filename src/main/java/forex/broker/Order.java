package forex.broker;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import java.time.LocalDateTime;
import java.util.Objects;

public abstract class Order {

    private final String orderId;
    private final LocalDateTime createTime;
    private final LocalDateTime canceledTime;
    private final LocalDateTime filledTime;

    Order(String orderId, LocalDateTime createTime, LocalDateTime canceledTime, LocalDateTime filledTime) {
        this.orderId = orderId;
        this.createTime = createTime;
        this.canceledTime = canceledTime;
        this.filledTime = filledTime;
    }

    public String getOrderId() {
        return orderId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getCanceledTime() {
        return canceledTime;
    }

    public LocalDateTime getFilledTime() {
        return filledTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order that = (Order) o;
        return Objects.equals(orderId, that.orderId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(canceledTime, that.canceledTime) &&
                Objects.equals(filledTime, that.filledTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, createTime, canceledTime, filledTime);
    }

    @Override
    public final String toString() {
        return toStringHelper().toString();
    }

    protected ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this)
                .add("orderId", orderId)
                .add("createTime", createTime)
                .add("canceledTime", canceledTime)
                .add("filledTime", filledTime);
    }
}
