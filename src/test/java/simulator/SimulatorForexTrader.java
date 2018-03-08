package simulator;

import broker.Context;
import broker.OpenPositionRequest;
import live.OandaTrader;
import market.ForexPortfolio;
import market.ForexPortfolioValue;
import market.InstrumentHistoryService;
import market.MarketTime;
import trader.TradingStrategy;

import static java.util.Collections.emptySet;
import static java.util.Collections.emptySortedSet;

public class SimulatorForexTrader extends OandaTrader {

    private ForexPortfolioValue drawdownPortfolio = null;
    private ForexPortfolioValue profitPortfolio = null;
    private ForexPortfolioValue mostRecentPortfolio = null;

    // This should be managed in the market
    private OpenPositionRequest openedPosition;

    public SimulatorForexTrader(String accountId, Context context, TradingStrategy tradingStrategy, MarketTime clock, InstrumentHistoryService instrumentHistoryService) throws Exception {
        super(accountId, context, tradingStrategy, clock, instrumentHistoryService);
        setPortfolio(new ForexPortfolio(0, emptySet(), emptySortedSet()));
    }

    public void addPortfolioValueSnapshot(ForexPortfolioValue portfolioValue) {
        if (drawdownPortfolio == null || drawdownPortfolio.pipettes() > portfolioValue.pipettes()) {
            drawdownPortfolio = portfolioValue;
        }
        if (profitPortfolio == null || profitPortfolio.pipettes() < portfolioValue.pipettes()) {
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
