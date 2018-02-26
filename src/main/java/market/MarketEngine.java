package market;

import broker.Broker;
import broker.PortfolioValue;
import broker.Position;
import broker.PositionValue;
import market.order.BuyMarketOrder;
import market.order.OrderRequest;
import market.order.OrderStatus;
import market.order.SellMarketOrder;
import org.slf4j.LoggerFactory;
import simulator.Simulation;
import simulator.SimulatorClock;

import java.util.HashMap;
import java.util.Map;

public interface MarketEngine<I extends Instrument> extends Market<I> {

    OrderRequest<I> getOrder(OrderRequest<I> order);

    OrderRequest<I> submit(BuyMarketOrder<I> p);

    OrderRequest<I> submit(SellMarketOrder<I> p);

    static <INSTRUMENT extends Instrument, MARKET extends Market<INSTRUMENT>, POSITION extends Position<INSTRUMENT>,
            POSITION_VALUE extends PositionValue<INSTRUMENT, POSITION>, PORTFOLIO_VALUE extends PortfolioValue<INSTRUMENT, POSITION>>
    MarketEngine<INSTRUMENT> create(Market<INSTRUMENT> market, Broker<INSTRUMENT, MARKET, POSITION, POSITION_VALUE, PORTFOLIO_VALUE> broker, SimulatorClock clock) {
        return new MarketEngineImpl<>(market, broker, clock);
    }

    class MarketEngineImpl<INSTRUMENT extends Instrument, MARKET extends Market<INSTRUMENT>, POSITION extends Position<INSTRUMENT>,
            POSITION_VALUE extends PositionValue<INSTRUMENT, POSITION>, PORTFOLIO_VALUE extends PortfolioValue<INSTRUMENT, POSITION>> implements MarketEngine<INSTRUMENT> {

        private final Market<INSTRUMENT> market;
        private final Broker<INSTRUMENT, MARKET, POSITION, POSITION_VALUE, PORTFOLIO_VALUE> broker;
        private final SimulatorClock clock;
        private final Map<String, OrderRequest<INSTRUMENT>> ordersById = new HashMap<>();

        MarketEngineImpl(Market<INSTRUMENT> market, Broker<INSTRUMENT, MARKET, POSITION, POSITION_VALUE, PORTFOLIO_VALUE> broker, SimulatorClock clock) {
            this.market = market;
            this.broker = broker;
            this.clock = clock;
        }

        @Override
        public double getPrice(INSTRUMENT instrument) {
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
        public OrderRequest<INSTRUMENT> getOrder(OrderRequest<INSTRUMENT> order) {
            return ordersById.get(order.getId());
        }

        @Override
        public OrderRequest<INSTRUMENT> submit(BuyMarketOrder<INSTRUMENT> order) {
            OrderRequest<INSTRUMENT> open = OrderRequest.open(order, clock);
            ordersById.put(open.getId(), open);

            processOrders();

            return open;
        }

        private void processOrders() {
            ordersById.values().stream()
                    .filter(it -> it.getStatus() == OrderStatus.OPEN)
                    .forEach(order -> {
                        double price = getPrice(order.getInstrument());
                        OrderRequest<INSTRUMENT> executed = OrderRequest.executed(order, clock, price);
                        ordersById.put(order.getId(), executed);

                        broker.orderFilled(executed);
                    });
        }

        @Override
        public OrderRequest<INSTRUMENT> submit(SellMarketOrder<INSTRUMENT> order) {
            OrderRequest<INSTRUMENT> open = OrderRequest.open(order, clock);
            ordersById.put(open.getId(), open);

            processOrders();

            return open;
        }
    }
}
