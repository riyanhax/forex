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

    OrderRequest submit(BuyMarketOrder p);

    OrderRequest submit(SellMarketOrder p);

    static MarketEngine create(ForexMarket market, ForexBroker broker, SimulatorClock clock) {
        return new MarketEngineImpl(market, broker, clock);
    }

    class MarketEngineImpl implements MarketEngine {

        private final ForexMarket market;
        private final ForexBroker broker;
        private final SimulatorClock clock;
        private final Map<String, OrderRequest> ordersById = new HashMap<>();

        MarketEngineImpl(ForexMarket market, ForexBroker broker, SimulatorClock clock) {
            this.market = market;
            this.broker = broker;
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

            market.init(simulation);
            broker.init(simulation);
        }

        @Override
        public void processUpdates() {

            if (!market.isAvailable()) {
                LoggerFactory.getLogger(MarketEngine.class).info("Broker is closed.");
                return;
            }

            market.processUpdates();

            processOrders();

            broker.processUpdates(this);
        }

        @Override
        public OrderRequest getOrder(OrderRequest order) {
            return ordersById.get(order.getId());
        }

        @Override
        public OrderRequest submit(BuyMarketOrder order) {
            OrderRequest open = OrderRequest.open(order, clock);
            ordersById.put(open.getId(), open);

            processOrders();

            return open;
        }

        private void processOrders() {
            ordersById.values().stream()
                    .filter(it -> it.getStatus() == OrderStatus.OPEN)
                    .forEach(order -> {
                        double price = getPrice(order.getInstrument());
                        OrderRequest executed = OrderRequest.executed(order, clock, price);
                        ordersById.put(order.getId(), executed);

                        broker.orderFilled(executed);
                    });
        }

        @Override
        public OrderRequest submit(SellMarketOrder order) {
            OrderRequest open = OrderRequest.open(order, clock);
            ordersById.put(open.getId(), open);

            processOrders();

            return open;
        }
    }
}
