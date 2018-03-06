package simulator;

import broker.OpenPositionRequest;
import market.ForexPortfolio;
import market.ForexPortfolioValue;
import market.InstrumentHistoryService;
import market.MarketTime;
import trader.BaseTrader;
import trader.TradingStrategy;

import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Collections.emptySortedSet;

public class SimulatorForexTrader extends BaseTrader {

    private final String accountNumber = UUID.randomUUID().toString();

    private ForexPortfolioValue drawdownPortfolio = null;
    private ForexPortfolioValue profitPortfolio = null;
    private ForexPortfolioValue mostRecentPortfolio = null;

    // This should be managed in the market
    private OpenPositionRequest openedPosition;

    public SimulatorForexTrader(TradingStrategy tradingStrategy, MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        super(tradingStrategy, clock, instrumentHistoryService);
        setPortfolio(new ForexPortfolio(0, emptySet(), emptySortedSet()));
    }

    @Override
    public String getAccountNumber() {
        return accountNumber;
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
