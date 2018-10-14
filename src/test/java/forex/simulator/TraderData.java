package forex.simulator;

import forex.market.AccountSnapshot;

public class TraderData {

    private AccountSnapshot drawdownPortfolio = null;
    private AccountSnapshot profitPortfolio = null;
    private AccountSnapshot mostRecentPortfolio = null;

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
