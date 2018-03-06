package simulator;

import broker.ForexBroker;
import broker.OpenPositionRequest;
import market.ForexPortfolio;
import market.ForexPortfolioValue;
import market.order.OrderRequest;
import trader.ForexTrader;

import static java.util.Collections.emptySet;
import static java.util.Collections.emptySortedSet;

public class SimulatorForexTrader implements ForexTrader {

    private final ForexTrader tradingStrategy;

    private ForexPortfolioValue drawdownPortfolio = null;
    private ForexPortfolioValue profitPortfolio = null;
    private ForexPortfolioValue mostRecentPortfolio = null;

    // This should be managed in the market
    private OpenPositionRequest openedPosition;

    public SimulatorForexTrader(ForexTrader tradingStrategy) {
        this.tradingStrategy = tradingStrategy;

        tradingStrategy.setPortfolio(new ForexPortfolio(0, emptySet(), emptySortedSet()));
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

    public void addPortfolioValueSnapshot(ForexPortfolioValue portfolioValue) {
        if (drawdownPortfolio == null || drawdownPortfolio.pips() > portfolioValue.pips()) {
            drawdownPortfolio = portfolioValue;
        }
        if (profitPortfolio == null || profitPortfolio.pips() < portfolioValue.pips()) {
            profitPortfolio = portfolioValue;
        }
        mostRecentPortfolio = portfolioValue;
    }

    public ForexPortfolioValue getDrawdownPortfolio() {
        return drawdownPortfolio;
    }

    public ForexPortfolioValue getProfitPortfolio() {
        return profitPortfolio;
    }

    public ForexPortfolioValue getMostRecentPortfolio() {
        return mostRecentPortfolio;
    }

    public OpenPositionRequest getOpenedPosition() {
        return openedPosition;
    }

    public void setOpenedPosition(OpenPositionRequest positionRequest) {
        this.openedPosition = positionRequest;
    }
}
