package market.order;

import java.util.Optional;

public interface MarketOrder extends Order {

    @Override
    default Optional<Long> limit() {
        return Optional.empty();
    }
}
