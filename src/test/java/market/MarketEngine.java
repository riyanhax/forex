package market;

import market.order.BuyLimitOrder;
import market.order.BuyMarketOrder;
import market.order.BuyStopOrder;
import market.order.Order;
import market.order.OrderRequest;
import market.order.OrderStatus;
import market.order.SellLimitOrder;
import market.order.SellMarketOrder;
import market.order.SellStopOrder;
import org.slf4j.LoggerFactory;
import simulator.Simulation;
import simulator.SimulatorForexBroker;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface MarketEngine extends Market {

    OrderRequest getOrder(OrderRequest order);

    OrderRequest submit(SimulatorForexBroker broker, BuyMarketOrder p);

    OrderRequest submit(SimulatorForexBroker broker, SellMarketOrder p);

    OrderRequest submit(SimulatorForexBroker broker, BuyLimitOrder p);

    OrderRequest submit(SimulatorForexBroker broker, SellLimitOrder p);

    OrderRequest submit(SimulatorForexBroker broker, BuyStopOrder order);

    OrderRequest submit(SimulatorForexBroker broker, SellStopOrder p);

    static MarketEngine create(ForexMarket market, MarketTime clock) {
        return new MarketEngineImpl(market, clock);
    }

    void cancel(OrderRequest cancel);

    class MarketEngineImpl implements MarketEngine {

        private final ForexMarket market;
        private final MarketTime clock;
        private final Set<String> openOrders = new LinkedHashSet<>();
        private final Set<String> canceledOrders = new LinkedHashSet<>();
        private final Set<String> stopOrders = new LinkedHashSet<>();
        private final Map<String, OrderRequest> ordersById = new HashMap<>();
        private final Map<String, SimulatorForexBroker> brokersByOrder = new HashMap<>();

        MarketEngineImpl(ForexMarket market, MarketTime clock) {
            this.market = market;
            this.clock = clock;
        }

        @Override
        public long getPrice(Instrument instrument) {
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
            openOrders.clear();
            canceledOrders.clear();
            stopOrders.clear();

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

        @Override
        public OrderRequest submit(SimulatorForexBroker broker, BuyStopOrder order) {
            return orderSubmitted(broker, order);
        }

        @Override
        public OrderRequest submit(SimulatorForexBroker broker, SellStopOrder order) {
            return orderSubmitted(broker, order);
        }

        @Override
        public void cancel(OrderRequest order) {
            canceledOrders.add(order.getId());
        }

        private OrderRequest orderSubmitted(SimulatorForexBroker broker, Order order) {
            OrderRequest open = OrderRequest.open(order, clock);
            addOrder(broker, open);

            return open;
        }

        private void processOrders() {
            for (Iterator<String> iter = stopOrders.iterator(); iter.hasNext(); ) {
                String orderId = iter.next();
                if (canceledOrders.contains(orderId)) {
                    continue;
                }
                OrderRequest order = ordersById.get(orderId);
                double price = getPrice(order.getInstrument());

                double stop = order.stop().get();
                if (order.isSellOrder() && price <= stop || order.isBuyOrder() && price >= stop) {
                    openOrders.add(orderId);
                    iter.remove();
                }
            }

            for (Iterator<String> iter = openOrders.iterator(); iter.hasNext(); ) {
                String orderId = iter.next();
                OrderRequest order = ordersById.get(orderId);

                final OrderRequest updated;

                if (order.isExpired(clock.now()) || canceledOrders.contains(orderId)) {
                    updated = OrderRequest.cancelled(order, clock);
                    canceledOrders.remove(orderId);
                } else {
                    long price = getPrice(order.getInstrument());
                    Optional<Long> limit = order.limit();
                    if (limit.isPresent()) {
                        long limitPrice = limit.get();
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

            openOrders.removeAll(canceledOrders);
        }

        private void addOrder(SimulatorForexBroker broker, OrderRequest order) {
            String id = order.getId();

            if (order.stop().isPresent()) {
                stopOrders.add(id);
            } else {
                openOrders.add(id);
            }
            ordersById.put(id, order);
            brokersByOrder.put(id, broker);
        }
    }
}
