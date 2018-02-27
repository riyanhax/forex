package market.order;

import broker.Expiry;

import java.util.Optional;

public interface MarketOrder extends Order {
    @Override
    default Expiry expiry() {
        return Expiry.NONE;
    }

    @Override
    default Optional<Double> limit() {
        return Optional.empty();
    }
}
