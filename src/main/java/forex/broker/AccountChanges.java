package forex.broker;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

public class AccountChanges {

    private final List<TradeSummary> tradesClosed;
    private final List<TradeSummary> tradesOpened;

    public AccountChanges(List<TradeSummary> tradesClosed, List<TradeSummary> tradesOpened) {
        this.tradesClosed = tradesClosed;
        this.tradesOpened = tradesOpened;
    }

    public List<TradeSummary> getTradesClosed() {
        return tradesClosed;
    }

    public List<TradeSummary> getTradesOpened() {
        return tradesOpened;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountChanges that = (AccountChanges) o;
        return Objects.equals(tradesClosed, that.tradesClosed) &&
                Objects.equals(tradesOpened, that.tradesOpened);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradesClosed, tradesOpened);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tradesClosed", tradesClosed)
                .add("tradesOpened", tradesOpened)
                .toString();
    }

    AccountChanges tradeOpened(TradeSummary filledPosition) {
        List<TradeSummary> newTradesOpened = new ArrayList<>(tradesOpened);
        newTradesOpened.add(filledPosition);

        return new AccountChanges(tradesClosed, newTradesOpened);
    }

    AccountChanges tradeClosed(TradeSummary filledPosition) {
        List<TradeSummary> newTradesClosed = new ArrayList<>(tradesClosed);
        newTradesClosed.add(filledPosition);

        return new AccountChanges(newTradesClosed, tradesOpened);
    }

    static AccountChanges empty() {
        return new AccountChanges(emptyList(), emptyList());
    }
}
