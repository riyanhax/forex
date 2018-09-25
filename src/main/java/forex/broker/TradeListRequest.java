package forex.broker;

import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptySet;

public class TradeListRequest {
    private final String accountID;
    private final TradeStateFilter filter;
    private final int count;
    private final Set<String> tradeIds;

    public TradeListRequest(String accountID, TradeStateFilter filter, Iterable<String> tradeIds) {
        this(accountID, filter, 50, tradeIds);
    }

    public TradeListRequest(String accountID, TradeStateFilter filter, int count) {
        this(accountID, filter, count, emptySet());
    }

    private TradeListRequest(String accountID, TradeStateFilter filter, int count, Iterable<String> tradeIds) {
        Objects.requireNonNull(filter, "Filter cannot be null!");

        this.accountID = accountID;
        this.filter = filter;
        this.count = count;
        this.tradeIds = ImmutableSet.copyOf(tradeIds);
    }

    public String getAccountID() {
        return accountID;
    }

    public TradeStateFilter getFilter() {
        return filter;
    }

    public int getCount() {
        return count;
    }

    public Set<String> getTradeIds() {
        return tradeIds;
    }
}
