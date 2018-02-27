package market.order;

import java.util.Optional;

public interface MarketOrder extends Order {
    @Override
    default Optional<Double> limit() {
        return Optional.empty();
    }
}
