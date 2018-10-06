package forex.broker;

import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;
import java.util.Objects;

public class OrderFillTransaction {

    private final String orderID;
    private final String id;
    private final LocalDateTime time;

    public OrderFillTransaction(String orderID, String id, LocalDateTime time) {
        this.orderID = orderID;
        this.id = id;
        this.time = time;
    }

    public String getOrderID() {
        return orderID;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderFillTransaction that = (OrderFillTransaction) o;
        return Objects.equals(orderID, that.orderID) &&
                Objects.equals(id, that.id) &&
                Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderID, id, time);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("orderID", orderID)
                .add("id", id)
                .add("time", time)
                .toString();
    }
}
