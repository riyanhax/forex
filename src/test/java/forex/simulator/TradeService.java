package forex.simulator;

import java.util.SortedSet;

public interface TradeService {

    SortedSet<TradeHistory> tradeClosed(String accountID, TradeHistory closedTrade);

    SortedSet<TradeHistory> getClosedTradesForAccountID(String id);
}
