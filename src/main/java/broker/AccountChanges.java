package broker;

import java.util.List;

public class AccountChanges {

    private final List<TradeSummary> tradesClosed;

    public AccountChanges(List<TradeSummary> tradesClosed) {
        this.tradesClosed = tradesClosed;
    }

    public List<TradeSummary> getTradesClosed() {
        return tradesClosed;
    }
}
