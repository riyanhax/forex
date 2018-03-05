package market;

import market.order.BuyLimitOrder;
import market.order.BuyMarketOrder;
import market.order.Order;
import market.order.OrderRequest;
import market.order.OrderStatus;
import market.order.SellLimitOrder;
import market.order.SellMarketOrder;
import org.slf4j.LoggerFactory;
import simulator.Simulation;
import simulator.SimulatorForexBroker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MarketEngine extends Market {

    OrderRequest getOrder(OrderRequest order);

    OrderRequest submit(SimulatorForexBroker broker, BuyMarketOrder p);

    OrderRequest submit(SimulatorForexBroker broker, SellMarketOrder p);

    OrderRequest submit(SimulatorForexBroker broker, BuyLimitOrder p);

    OrderRequest submit(SimulatorForexBroker broker, SellLimitOrder p);

    static MarketEngine create(ForexMarket market, MarketTime clock) {
        return new MarketEngineImpl(market, clock);
    }

    class MarketEngineImpl implements MarketEngine {

        private final ForexMarket market;
        private final MarketTime clock;
        private final List<String> openOrders = new ArrayList<>();
        private final Map<String, OrderRequest> ordersById = new HashMap<>();
        private final Map<String, SimulatorForexBroker> brokersByOrder = new HashMap<>();

        MarketEngineImpl(ForexMarket market, MarketTime clock) {
            this.market = market;
            this.clock = clock;
        }

        @Override
        public double getPrice(Instrument instrument) {
            return market.getPrice(instrument);
        }

        @Override
        public boolean isAvailable() {
            return market.isAvailable();
        }

        @Override
        public boolean isAvailable(LocalDate date) {
            return market.isAvailable(date);
        }

        @Override
        public void init(Simulation simulation) {
            ordersById.clear();
            brokersByOrder.clear();

            market.init(simulation);
        }

        @Override
        public void processUpdates() {

            if (!market.isAvailable()) {
                LoggerFactory.getLogger(MarketEngine.class).debug("Market is closed.");
                return;
            }

            market.processUpdates();
            processOrders();
        }

        @Override
        public OrderRequest getOrder(OrderRequest order) {
            return ordersById.get(order.getId());
        }

        @Override
        public OrderRequest submit(SimulatorForexBroker broker, BuyMarketOrder order) {
            return orderSubmitted(broker, order);
        }

        @Override
        public OrderRequest submit(SimulatorForexBroker broker, BuyLimitOrder order) {
            return orderSubmitted(broker, order);
        }

        @Override
        public OrderRequest submit(SimulatorForexBroker broker, SellMarketOrder order) {
            return orderSubmitted(broker, order);
        }

        @Override
        public OrderRequest submit(SimulatorForexBroker broker, SellLimitOrder order) {
            return orderSubmitted(broker, order);
        }

        private OrderRequest orderSubmitted(SimulatorForexBroker broker, Order order) {
            OrderRequest open = OrderRequest.open(order, clock);
            addOrder(broker, open);

            return open;
        }

        private void processOrders() {
            for (Iterator<String> iter = openOrders.iterator(); iter.hasNext(); ) {
                String orderId = iter.next();
                OrderRequest order = ordersById.get(orderId);

                final OrderRequest updated;

                if (order.isExpired(clock.now())) {
                    updated = OrderRequest.cancelled(order, clock);
                } else {
                    double price = getPrice(order.getInstrument());
                    Optional<Double> limit = order.limit();
                    if (limit.isPresent()) {
                        double limitPrice = limit.get();
                        if ((order.isBuyOrder() && price > limitPrice) ||
                                (order.isSellOrder() && price < limitPrice)) {
                            continue; // Limit not met
                        }
                    }

                    updated = OrderRequest.executed(order, clock, price);
                }

                ordersById.put(orderId, updated);
                iter.remove();

                SimulatorForexBroker broker = brokersByOrder.get(orderId);

                if (updated.getStatus() == OrderStatus.CANCELLED) {
                    broker.orderCancelled(updated);
                } else {
                    broker.orderFilled(updated);
                }
            }
        }

        private void addOrder(SimulatorForexBroker broker, OrderRequest order) {
            String id = order.getId();

            openOrders.add(id);
            ordersById.put(id, order);
            brokersByOrder.put(id, broker);
        }
    }
}