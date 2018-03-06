package trader;

import broker.ForexBroker;
import market.ForexPortfolio;
import market.order.OrderRequest;

public interface ForexTrader {

    String getAccountNumber();

    void processUpdates(ForexBroker broker) throws Exception;

    void cancelled(OrderRequest cancelled);

    ForexPortfolio getPortfolio();

    void setPortfolio(ForexPortfolio portfolio);
}
