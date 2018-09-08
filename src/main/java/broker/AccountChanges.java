package broker;

import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.Objects;

public class AccountChanges {

    private final List<TradeSummary> tradesClosed;

    public AccountChanges(List<TradeSummary> tradesClosed) {
        this.tradesClosed = tradesClosed;
    }

    public List<TradeSummary> getTradesClosed() {
        return tradesClosed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountChanges that = (AccountChanges) o;
        return Objects.equals(tradesClosed, that.tradesClosed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradesClosed);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tradesClosed", tradesClosed)
                .toString();
    }
}
