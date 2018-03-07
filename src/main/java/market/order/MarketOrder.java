package market.order;

import broker.Expiry;

import java.util.Optional;

public interface MarketOrder extends Order {

    @Override
    default Optional<Expiry> expiry() {
        return Optional.empty();
    }

    @Override
    default Optional<Long> limit() {
        return Optional.empty();
    }
}
