package forex.broker;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class TakeProfitDetails {
    private long price;

    public void setPrice(long price) {
        this.price = price;
    }

    public long getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TakeProfitDetails that = (TakeProfitDetails) o;
        return price == that.price;
    }

    @Override
    public int hashCode() {
        return Objects.hash(price);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("price", price)
                .toString();
    }
}
