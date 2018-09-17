package forex.simulator;

import forex.broker.MarketOrderRequest;
import forex.market.AccountSnapshot;

public class TraderData {

    private AccountSnapshot drawdownPortfolio = null;
    private AccountSnapshot profitPortfolio = null;
    private AccountSnapshot mostRecentPortfolio = null;

    // This should be managed in the market
    private MarketOrderRequest openedPosition;

    public void addSnapshot(AccountSnapshot accountSnapshot) {
        if (drawdownPortfolio == null || drawdownPortfolio.pipettes() > accountSnapshot.pipettes()) {
            drawdownPortfolio = accountSnapshot;
        }
        if (profitPortfolio == null || profitPortfolio.pipettes() < accountSnapshot.pipettes()) {
            profitPortfolio = accountSnapshot;
        }
        mostRecentPortfolio = accountSnapshot;
    }

    public AccountSnapshot getDrawdownPortfolio() {
        return drawdownPortfolio;
    }

    public AccountSnapshot getProfitPortfolio() {
        return profitPortfolio;
    }

    public AccountSnapshot getMostRecentPortfolio() {
        return mostRecentPortfolio;
    }
}
