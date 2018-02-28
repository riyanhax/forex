package trader.forex;

import broker.forex.ForexBroker;
import broker.forex.ForexPortfolio;
import broker.forex.ForexPortfolioValue;
import market.order.OrderRequest;
import simulator.Simulation;

public interface ForexTrader {

    String getAccountNumber();

    void processUpdates(ForexBroker broker);

    void cancelled(OrderRequest cancelled);

    void init(Simulation simulation);

    ForexPortfolio getPortfolio();

    void setPortfolio(ForexPortfolio portfolio);

    void addPortfolioValueSnapshot(ForexPortfolioValue portfolioValue);

    ForexPortfolioValue getDrawdownPortfolio();

    ForexPortfolioValue getProfitPortfolio();

    ForexPortfolioValue getMostRecentPortfolio();

}
