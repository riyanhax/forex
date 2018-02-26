package market.order;

import market.Instrument;
import simulator.SimulatorClock;

import java.time.LocalDateTime;
import java.util.UUID;

public interface OrderRequest<I extends Instrument> extends Order<I> {
    String getId();

    LocalDateTime getSubmissionDate();

    OrderStatus getStatus();

    double getExecutionPrice();

    LocalDateTime getProcessedDate();

    static <I extends Instrument> OrderRequest<I> open(Order<I> order, SimulatorClock clock) {
        return new OrderRequestImpl<>(order, UUID.randomUUID().toString(), clock.now());
    }

    static <I extends Instrument> OrderRequest<I> executed(OrderRequest<I> order, SimulatorClock clock, double price) {
        return new OrderRequestImpl<>(order, order.getId(), clock.now(), OrderStatus.EXECUTED, clock.now(), price);
    }

    class OrderRequestImpl<I extends Instrument> implements OrderRequest<I> {
        private final Order<I> order;
        private final String id;
        private final LocalDateTime submissionDate;
        private final LocalDateTime processedDate;
        private final double executionPrice;
        private OrderStatus status;

        public OrderRequestImpl(Order<I> order, String id, LocalDateTime submissionDate) {
            this(order, id, submissionDate, OrderStatus.OPEN, null, -1);
        }

        public OrderRequestImpl(Order<I> order, String id, LocalDateTime submissionDate, OrderStatus status, LocalDateTime processedDate, double executionPrice) {
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
        public I getInstrument() {
            return order.getInstrument();
        }

        @Override
        public int getUnits() {
            return order.getUnits();
        }
    }
}
