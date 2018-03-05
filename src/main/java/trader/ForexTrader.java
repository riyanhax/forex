package trader;

import broker.ForexBroker;
import broker.OpenPositionRequest;
import market.ForexPortfolio;
import market.ForexPortfolioValue;
import market.order.OrderRequest;

public interface ForexTrader {

    String getAccountNumber();

    void processUpdates(ForexBroker broker) throws Exception;

    void cancelled(OrderRequest cancelled);

    ForexPortfolio getPortfolio();

    void setPortfolio(ForexPortfolio portfolio);

    void addPortfolioValueSnapshot(ForexPortfolioValue portfolioValue);

    ForexPortfolioValue getDrawdownPortfolio();

    ForexPortfolioValue getProfitPortfolio();

    ForexPortfolioValue getMostRecentPortfolio();

    OpenPositionRequest getOpenedPosition();

    void setOpenedPosition(OpenPositionRequest positionRequest);
}
