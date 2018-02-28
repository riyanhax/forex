package market;

import broker.forex.ForexBroker;
import market.forex.ForexMarket;
import market.forex.Instrument;
import market.order.BuyLimitOrder;
import market.order.BuyMarketOrder;
import market.order.Order;
import market.order.OrderRequest;
import market.order.OrderStatus;
import market.order.SellLimitOrder;
import market.order.SellMarketOrder;
import org.slf4j.LoggerFactory;
import simulator.Simulation;
import simulator.SimulatorClock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface MarketEngine extends Market {

    OrderRequest getOrder(OrderRequest order);

    OrderRequest submit(ForexBroker broker, BuyMarketOrder p);

    OrderRequest submit(ForexBroker broker, SellMarketOrder p);

    OrderRequest submit(ForexBroker broker, BuyLimitOrder p);

    OrderRequest submit(ForexBroker broker, SellLimitOrder p);

    static MarketEngine create(ForexMarket market, SimulatorClock clock) {
        return new MarketEngineImpl(market, clock);
    }

    class MarketEngineImpl implements MarketEngine {

        private final ForexMarket market;
        private final SimulatorClock clock;
        private final Map<String, OrderRequest> ordersById = new HashMap<>();
        private final Map<String, ForexBroker> brokersByOrder = new HashMap<>();

        MarketEngineImpl(ForexMarket market, SimulatorClock clock) {
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
        public OrderRequest submit(ForexBroker broker, BuyMarketOrder order) {
            return orderSubmitted(broker, order);
        }

        @Override
        public OrderRequest submit(ForexBroker broker, BuyLimitOrder order) {
            return orderSubmitted(broker, order);
        }

        @Override
        public OrderRequest submit(ForexBroker broker, SellMarketOrder order) {
            return orderSubmitted(broker, order);
        }

        @Override
        public OrderRequest submit(ForexBroker broker, SellLimitOrder order) {
            return orderSubmitted(broker, order);
        }

        private OrderRequest orderSubmitted(ForexBroker broker, Order order) {
            OrderRequest open = OrderRequest.open(order, clock);
            addOrder(broker, open);

            return open;
        }

        private void processOrders() {
            Collection<OrderRequest> orders = ordersById.values();
            for (OrderRequest order : orders) {
                if (order.getStatus() == OrderStatus.OPEN) {
                    String orderId = order.getId();
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

                    ForexBroker broker = brokersByOrder.get(orderId);

                    if (updated.getStatus() == OrderStatus.CANCELLED) {
                        broker.orderCancelled(updated);
                    } else {
                        broker.orderFilled(updated);
                    }
                }
            }
        }

        private void addOrder(ForexBroker broker, OrderRequest order) {
            ordersById.put(order.getId(), order);
            brokersByOrder.put(order.getId(), broker);
        }
    }
}
