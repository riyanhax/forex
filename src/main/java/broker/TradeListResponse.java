package broker;

import java.util.List;
import java.util.Optional;

public class TradeListResponse {

    // TODO: Create our version of Oanda Trade class
    private final List<TradeSummary> trades;
    private final TransactionID lastTransactionID;

    public TradeListResponse(List<TradeSummary> trades, TransactionID lastTransactionID) {
        this.trades = trades;
        this.lastTransactionID = lastTransactionID;
    }

    public List<TradeSummary> getTrades() {
        return trades;
    }

    public Optional<TransactionID> getLastTransactionID() {
        return Optional.ofNullable(lastTransactionID);
    }
}
