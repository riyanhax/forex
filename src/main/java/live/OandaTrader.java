package live;

import broker.ForexBroker;
import broker.OpenPositionRequest;
import market.ForexPortfolio;
import market.ForexPortfolioValue;
import market.order.OrderRequest;
import trader.ForexTrader;

public class OandaTrader implements ForexTrader {

    private final ForexTrader tradingStrategy;

    public OandaTrader(ForexTrader tradingStrategy) {
        this.tradingStrategy = tradingStrategy;
    }

    @Override
    public String getAccountNumber() {
        return tradingStrategy.getAccountNumber();
    }

    @Override
    public void processUpdates(ForexBroker broker) throws Exception {
        tradingStrategy.processUpdates(broker);
    }

    @Override
    public void cancelled(OrderRequest cancelled) {
        tradingStrategy.cancelled(cancelled);
    }

    @Override
    public ForexPortfolio getPortfolio() {
        return tradingStrategy.getPortfolio();
    }

    @Override
    public void setPortfolio(ForexPortfolio portfolio) {
        tradingStrategy.setPortfolio(portfolio);
    }

    @Override
    public void addPortfolioValueSnapshot(ForexPortfolioValue portfolioValue) {
        tradingStrategy.addPortfolioValueSnapshot(portfolioValue);
    }

    @Override
    public ForexPortfolioValue getDrawdownPortfolio() {
        return tradingStrategy.getDrawdownPortfolio();
    }

    @Override
    public ForexPortfolioValue getProfitPortfolio() {
        return tradingStrategy.getProfitPortfolio();
    }

    @Override
    public ForexPortfolioValue getMostRecentPortfolio() {
        return tradingStrategy.getMostRecentPortfolio();
    }

    @Override
    public OpenPositionRequest getOpenedPosition() {
        return tradingStrategy.getOpenedPosition();
    }

    @Override
    public void setOpenedPosition(OpenPositionRequest positionRequest) {
        tradingStrategy.setOpenedPosition(positionRequest);
    }
}
