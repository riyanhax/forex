package forex.simulator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import forex.broker.AccountSummary;
import forex.broker.TradeSummary;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import static java.util.Comparator.comparing;

@Service
class MapBasedTradeService implements TradeService {

    private final Map<String, SortedSet<TradeHistory>> closedTrades = new HashMap<>();
    private final Map<String, AccountSummary> mostRecentPortfolio = new HashMap<>();

    @Override
    public SortedSet<TradeHistory> tradeClosed(String accountID, TradeHistory closedTrade) {
        SortedSet<TradeHistory> closedTradesForAccount = closedTrades.get(accountID);
        closedTradesForAccount.add(closedTrade);

        closedTrades.put(accountID, closedTradesForAccount);

        return ImmutableSortedSet.copyOf(comparator(), closedTradesForAccount);
    }

    @Override
    public SortedSet<TradeHistory> getClosedTradesForAccountID(String id) {
        closedTrades.computeIfAbsent(id, it -> new TreeSet<>(comparator()));

        return ImmutableSortedSet.copyOf(comparator(), closedTrades.get(id));
    }

    @Override
    public SortedSet<TradeSummary> getOpenTradesForAccountID(String id) {
        return ImmutableSortedSet.copyOf(Comparator.comparing(TradeSummary::getOpenTime),
                mostRecentPortfolio.get(id).getTrades());
    }

    @Override
    public List<AccountSummary> accounts() {
        return ImmutableList.copyOf(mostRecentPortfolio.values());
    }

    @Override
    public void update(AccountSummary account) {
        mostRecentPortfolio.put(account.getId(), account);
    }

    @Override
    public AccountSummary getAccount(String id, Function<String, AccountSummary> initialAccountCreator) {

        mostRecentPortfolio.computeIfAbsent(id, initialAccountCreator);

        return mostRecentPortfolio.get(id);
    }

    private Comparator<TradeHistory> comparator() {
        return comparing(TradeHistory::getOpenTime);
    }
}
