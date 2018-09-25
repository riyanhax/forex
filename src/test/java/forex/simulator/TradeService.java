package forex.simulator;

import forex.broker.AccountSummary;
import forex.broker.TradeSummary;

import java.util.List;
import java.util.SortedSet;
import java.util.function.Function;

public interface TradeService {

    SortedSet<TradeHistory> tradeClosed(String accountID, TradeHistory closedTrade);

    SortedSet<TradeHistory> getClosedTradesForAccountID(String id);

    SortedSet<TradeSummary> getOpenTradesForAccountID(String id);

    List<AccountSummary> accounts();

    void update(AccountSummary account);

    AccountSummary getAccount(String id, Function<String, AccountSummary> initialAccountCreator);

}
