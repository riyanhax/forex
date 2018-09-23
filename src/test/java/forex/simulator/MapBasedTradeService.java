package forex.simulator;

import com.google.common.collect.ImmutableSortedSet;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Comparator.comparing;

@Service
class MapBasedTradeService implements TradeService {

    private final Map<String, SortedSet<TradeHistory>> closedTrades = new HashMap<>();

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

    private Comparator<TradeHistory> comparator() {
        return comparing(TradeHistory::getOpenTime);
    }
}
