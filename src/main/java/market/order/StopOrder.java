package market.order;

import broker.Expiry;

import java.util.Optional;

public interface StopOrder extends Order {

    Optional<Double> stop();

    @Override
    default Optional<Expiry> expiry() {
        return Optional.empty();
    }

    @Override
    default Optional<Double> limit() {
        return Optional.empty();
    }
}
