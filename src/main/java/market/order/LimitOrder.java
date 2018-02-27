package market.order;

import broker.Expiry;

import java.util.Optional;

public interface LimitOrder extends Order {

    @Override
    default Optional<Expiry> expiry() {
        return Optional.empty();
    }
}
