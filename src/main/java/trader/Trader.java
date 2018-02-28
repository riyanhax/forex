package trader;

import broker.forex.ForexBroker;
import broker.forex.ForexPortfolio;
import broker.forex.ForexPortfolioValue;
import market.order.OrderRequest;
import simulator.Simulation;

import java.util.SortedSet;

public interface Trader {

    String getAccountNumber();

    void processUpdates(ForexBroker broker);

    void cancelled(OrderRequest cancelled);

    void init(Simulation simulation);

    ForexPortfolio getPortfolio();

    void setPortfolio(ForexPortfolio portfolio);

    void addPortfolioValueSnapshot(ForexPortfolioValue portfolioValue);

    SortedSet<ForexPortfolioValue> portfolioSnapshots();
}
