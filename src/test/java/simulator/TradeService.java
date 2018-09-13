package simulator;

import broker.AccountID;

import java.util.SortedSet;

public interface TradeService {

    SortedSet<TradeHistory> tradeClosed(AccountID accountID, TradeHistory closedTrade);

    SortedSet<TradeHistory> getClosedTradesForAccountID(String id);
}