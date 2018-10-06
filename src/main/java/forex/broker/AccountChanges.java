package forex.broker;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

public class AccountChanges {

    private final List<String> filledOrders;
    private final List<String> canceledOrders;
    private final List<TradeSummary> tradesClosed;
    private final List<TradeSummary> tradesOpened;

    public AccountChanges(List<String> filledOrders, List<String> canceledOrders,
                          List<TradeSummary> tradesClosed, List<TradeSummary> tradesOpened) {
        this.filledOrders = filledOrders;
        this.canceledOrders = canceledOrders;
        this.tradesClosed = tradesClosed;
        this.tradesOpened = tradesOpened;
    }

    public List<String> getFilledOrders() {
        return filledOrders;
    }

    public List<String> getCanceledOrders() {
        return canceledOrders;
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
        return Objects.equals(filledOrders, that.filledOrders) &&
                Objects.equals(canceledOrders, that.canceledOrders) &&
                Objects.equals(tradesClosed, that.tradesClosed) &&
                Objects.equals(tradesOpened, that.tradesOpened);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filledOrders, canceledOrders, tradesClosed, tradesOpened);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("filledOrders", filledOrders)
                .add("canceledOrders", canceledOrders)
                .add("tradesClosed", tradesClosed)
                .add("tradesOpened", tradesOpened)
                .toString();
    }

    AccountChanges tradeOpened(TradeSummary filledPosition) {
        List<TradeSummary> newTradesOpened = new ArrayList<>(tradesOpened);
        newTradesOpened.add(filledPosition);

        return new AccountChanges(filledOrders, canceledOrders, tradesClosed, newTradesOpened);
    }

    AccountChanges tradeClosed(TradeSummary filledPosition) {
        List<TradeSummary> newTradesClosed = new ArrayList<>(tradesClosed);
        newTradesClosed.add(filledPosition);

        return new AccountChanges(filledOrders, canceledOrders, newTradesClosed, tradesOpened);
    }

    static AccountChanges empty() {
        return new AccountChanges(emptyList(), emptyList(), emptyList(), emptyList());
    }
}
