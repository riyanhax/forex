package forex.broker;

import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.Objects;

public class PricingGetResponse {
    private final List<Price> prices;

    public PricingGetResponse(List<Price> prices) {
        this.prices = prices;
    }

    public List<Price> getPrices() {
        return prices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PricingGetResponse response = (PricingGetResponse) o;
        return Objects.equals(prices, response.prices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prices);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("prices", prices)
                .toString();
    }
}
