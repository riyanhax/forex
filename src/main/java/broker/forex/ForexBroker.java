package broker.forex;

import broker.Quote;
import market.forex.Instrument;
import market.order.OrderRequest;
import simulator.Simulation;
import trader.forex.ForexTrader;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;

public interface ForexBroker {
    void init(Simulation simulation, List<ForexTrader> traders);

    void processUpdates();

    ForexPortfolioValue getPortfolioValue(ForexTrader trader);

    Quote getQuote(Instrument pair);

    void orderCancelled(OrderRequest filled);

    void orderFilled(OrderRequest filled);

    boolean isOpen();

    boolean isOpen(LocalDate time);

    void openPosition(ForexTrader trader, Instrument pair, @Nullable Double limit);

    void closePosition(ForexTrader trader, ForexPosition position, @Nullable Double limit);

    OrderRequest getOrder(OrderRequest order);
}
