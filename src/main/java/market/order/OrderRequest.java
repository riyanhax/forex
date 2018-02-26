package market.order;

import market.forex.Instrument;
import simulator.SimulatorClock;

import java.time.LocalDateTime;
import java.util.UUID;

public interface OrderRequest extends Order {
    String getId();

    LocalDateTime getSubmissionDate();

    OrderStatus getStatus();

    double getExecutionPrice();

    LocalDateTime getProcessedDate();

    static OrderRequest open(Order order, SimulatorClock clock) {
        return new OrderRequestImpl(order, UUID.randomUUID().toString(), clock.now());
    }

    static OrderRequest executed(OrderRequest order, SimulatorClock clock, double price) {
        return new OrderRequestImpl(order, order.getId(), clock.now(), OrderStatus.EXECUTED, clock.now(), price);
    }

    class OrderRequestImpl implements OrderRequest {
        private final Order order;
        private final String id;
        private final LocalDateTime submissionDate;
        private final LocalDateTime processedDate;
        private final double executionPrice;
        private OrderStatus status;

        public OrderRequestImpl(Order order, String id, LocalDateTime submissionDate) {
            this(order, id, submissionDate, OrderStatus.OPEN, null, -1);
        }

        public OrderRequestImpl(Order order, String id, LocalDateTime submissionDate, OrderStatus status, LocalDateTime processedDate, double executionPrice) {
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
    }
}
