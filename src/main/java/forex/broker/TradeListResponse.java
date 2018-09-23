package forex.broker;

import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TradeListResponse {

    private final List<Trade> trades;
    private final String lastTransactionID;

    public TradeListResponse(List<Trade> trades, String lastTransactionID) {
        this.trades = trades;
        this.lastTransactionID = lastTransactionID;
    }

    public List<Trade> getTrades() {
        return trades;
    }

    public Optional<String> getLastTransactionID() {
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
