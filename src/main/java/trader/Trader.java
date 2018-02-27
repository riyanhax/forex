package trader;

import broker.forex.ForexBroker;
import broker.forex.ForexPortfolio;
import market.order.OrderRequest;
import simulator.Simulation;

public interface Trader {

    String getAccountNumber();

    void processUpdates(ForexBroker broker);

    void cancelled(OrderRequest cancelled);

    void init(Simulation simulation);

    ForexPortfolio getPortfolio();

    void setPortfolio(ForexPortfolio portfolio);
}
