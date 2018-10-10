package forex.broker;

import com.google.common.base.MoreObjects.ToStringHelper;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "account_stop_loss_order")
public class StopLossOrder extends Order {

    @Column(nullable = false)
    private long price;

    public StopLossOrder() {
    }

    public StopLossOrder(String orderId, String accountId, LocalDateTime createTime, LocalDateTime canceledTime, LocalDateTime filledTime, long price) {
        super(orderId, accountId, createTime, canceledTime, filledTime);

        this.price = price;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StopLossOrder that = (StopLossOrder) o;
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
