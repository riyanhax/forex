package market.order;

import broker.Expiry;
import market.Instrument;
import market.MarketTime;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OrderRequest extends Order {
    String getId();

    LocalDateTime getSubmissionDate();

    OrderStatus getStatus();

    Optional<Long> getExecutionPrice();

    LocalDateTime getProcessedDate();

    default boolean isExpired(LocalDateTime now) {
        Optional<Expiry> expiry = expiry();
        return expiry.isPresent() && !expiry.get().getExpiration(getSubmissionDate()).isAfter(now);
    }

    static OrderRequest open(Order order, MarketTime clock) {
        return new OrderRequestImpl(order, UUID.randomUUID().toString(), clock.now());
    }

    static OrderRequest executed(OrderRequest order, MarketTime clock, long price) {
        return new OrderRequestImpl(order, order.getId(), clock.now(), OrderStatus.EXECUTED, clock.now(), price);
    }

    static OrderRequest cancelled(OrderRequest order, MarketTime clock) {
        return new OrderRequestImpl(order, order.getId(), clock.now(), OrderStatus.CANCELLED, clock.now(), null);
    }

    class OrderRequestImpl implements OrderRequest {
        private final Order order;
        private final String id;
        private final LocalDateTime submissionDate;
        private final LocalDateTime processedDate;
        private final Long executionPrice;
        private final OrderStatus status;

        public OrderRequestImpl(Order order, String id, LocalDateTime submissionDate) {
            this(order, id, submissionDate, OrderStatus.OPEN, null, null);
        }

        public OrderRequestImpl(Order order, String id, LocalDateTime submissionDate, OrderStatus status, @Nullable LocalDateTime processedDate, @Nullable Long executionPrice) {
            this.order = order;
            this.id = id;
            this.submissionDate = submissionDate;
            this.status = status;
            this.processedDate = processedDate;
            this.executionPrice = executionPrice;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public LocalDateTime getSubmissionDate() {
            return submissionDate;
        }

        @Override
        public OrderStatus getStatus() {
            return status;
        }

        @Override
        public Optional<Long> getExecutionPrice() {
            return Optional.ofNullable(executionPrice);
        }

        @Override
        public LocalDateTime getProcessedDate() {
            return processedDate;
        }

        @Override
        public Instrument getInstrument() {
            return order.getInstrument();
        }

        @Override
        public int getUnits() {
            return order.getUnits();
        }

        @Override
        public Optional<Expiry> expiry() {
            return order.expiry();
        }

        @Override
        public Optional<Long> limit() {
            return order.limit();
        }

        @Override
        public boolean isSellOrder() {
            return order.isSellOrder();
        }

        @Override
        public boolean isBuyOrder() {
            return order.isBuyOrder();
        }
    }
}
