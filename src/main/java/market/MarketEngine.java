package market;

import broker.forex.ForexBroker;
import market.forex.ForexMarket;
import market.forex.Instrument;
import market.order.BuyMarketOrder;
import market.order.OrderRequest;
import market.order.OrderStatus;
import market.order.SellMarketOrder;
import org.slf4j.LoggerFactory;
import simulator.Simulation;
import simulator.SimulatorClock;

import java.util.HashMap;
import java.util.Map;

public interface MarketEngine extends Market {

    OrderRequest getOrder(OrderRequest order);

    OrderRequest submit(ForexBroker broker, BuyMarketOrder p);

    OrderRequest submit(ForexBroker broker, SellMarketOrder p);

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
                LoggerFactory.getLogger(MarketEngine.class).info("Broker is closed.");
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
            OrderRequest open = OrderRequest.open(order, clock);
            addOrder(broker, open);

            processOrders();

            return open;
        }

        private void processOrders() {
            ordersById.values().stream()
                    .filter(it -> it.getStatus() == OrderStatus.OPEN)
                    .forEach(order -> {
                        if (order.expiry().isAfter(clock.now(), order)) {
                            double price = getPrice(order.getInstrument());
                            OrderRequest executed = OrderRequest.executed(order, clock, price);

                            String orderId = order.getId();
                            ordersById.put(orderId, executed);

                            ForexBroker forexBroker = brokersByOrder.get(orderId);
                            forexBroker.orderFilled(executed);
                        }
                    });
        }

        @Override
        public OrderRequest submit(ForexBroker broker, SellMarketOrder order) {
            OrderRequest open = OrderRequest.open(order, clock);
            addOrder(broker, open);

            processOrders();

            return open;
        }

        private void addOrder(ForexBroker broker, OrderRequest order) {
            ordersById.put(order.getId(), order);
            brokersByOrder.put(order.getId(), broker);
        }
    }
}
