package market.order;

import broker.Expiry;
import market.forex.Instrument;
import simulator.SimulatorClock;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OrderRequest extends Order {
    String getId();

    LocalDateTime getSubmissionDate();

    OrderStatus getStatus();

    double getExecutionPrice();

    LocalDateTime getProcessedDate();

    static OrderRequest open(Order order, SimulatorClock clock) {
        return new OrderRequestImpl(order, UUID.randomUUID().toString(), clock.now(), null);
    }

    static OrderRequest executed(OrderRequest order, SimulatorClock clock, double price) {
        return new OrderRequestImpl(order, order.getId(), clock.now(), OrderStatus.EXECUTED, clock.now(), price, order.limit().orElse(null));
    }

    class OrderRequestImpl implements OrderRequest {
        private final Order order;
        private final String id;
        private final LocalDateTime submissionDate;
        private final LocalDateTime processedDate;
        private final double executionPrice;
        private final OrderStatus status;
        private final Double limit;

        public OrderRequestImpl(Order order, String id, LocalDateTime submissionDate, Double limit) {
            this(order, id, submissionDate, OrderStatus.OPEN, null, -1, limit);
        }

        public OrderRequestImpl(Order order, String id, LocalDateTime submissionDate, OrderStatus status, LocalDateTime processedDate, double executionPrice, @Nullable Double limit) {
            this.order = order;
            this.id = id;
            this.submissionDate = submissionDate;
            this.status = status;
            this.processedDate = processedDate;
            this.executionPrice = executionPrice;
            this.limit = limit;
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
        public double getExecutionPrice() {
            return executionPrice;
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
        public Expiry expiry() {
            return order.expiry();
        }

        @Override
        public Optional<Double> limit() {
            return Optional.ofNullable(limit);
        }
    }
}
