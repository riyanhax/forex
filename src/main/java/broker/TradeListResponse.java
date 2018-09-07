package broker;

import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeListResponse that = (TradeListResponse) o;
        return Objects.equals(trades, that.trades) &&
                Objects.equals(lastTransactionID, that.lastTransactionID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trades, lastTransactionID);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("trades", trades)
                .add("lastTransactionID", lastTransactionID)
                .toString();
    }
}
