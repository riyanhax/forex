package forex.broker;

import com.google.common.base.MoreObjects.ToStringHelper;

import java.time.LocalDateTime;
import java.util.Objects;

public class TakeProfitOrder extends Order {

    private final long price;

    public TakeProfitOrder(String orderId, LocalDateTime createTime, LocalDateTime canceledTime, LocalDateTime filledTime, long price) {
        super(orderId, createTime, canceledTime, filledTime);

        this.price = price;
    }

    public long getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TakeProfitOrder that = (TakeProfitOrder) o;
        return price == that.price;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), price);
    }

    @Override
    public ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("price", price);
    }
}
