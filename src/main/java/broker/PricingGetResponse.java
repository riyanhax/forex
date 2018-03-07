package broker;

import java.util.List;

public class PricingGetResponse {
    private List<Price> prices;

    public PricingGetResponse(List<Price> prices) {
        this.prices = prices;
    }

    public List<Price> getPrices() {
        return prices;
    }
}
