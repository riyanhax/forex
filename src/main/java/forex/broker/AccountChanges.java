package forex.broker;

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

public class AccountChanges {

    private static final Logger LOG = LoggerFactory.getLogger(AccountChanges.class);

    private final Orders createdOrders;
    private final Orders filledOrders;
    private final Orders canceledOrders;
    private final List<TradeSummary> tradesClosed;
    private final List<TradeSummary> tradesOpened;
    // TODO: Add transactions here and store when retrieved

    public AccountChanges(Orders createdOrders, Orders filledOrders, Orders canceledOrders,
                          List<TradeSummary> tradesClosed, List<TradeSummary> tradesOpened) {
        this.createdOrders = createdOrders;
        this.filledOrders = filledOrders;
        this.canceledOrders = canceledOrders;
        this.tradesClosed = tradesClosed;
        this.tradesOpened = tradesOpened;
    }

    public Orders getCreatedOrders() {
        return createdOrders;
    }

    public Orders getFilledOrders() {
        return filledOrders;
    }

    public Orders getCanceledOrders() {
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
        return Objects.equals(createdOrders, that.createdOrders) &&
                Objects.equals(filledOrders, that.filledOrders) &&
                Objects.equals(canceledOrders, that.canceledOrders) &&
                Objects.equals(tradesClosed, that.tradesClosed) &&
                Objects.equals(tradesOpened, that.tradesOpened);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdOrders, filledOrders, canceledOrders, tradesClosed, tradesOpened);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("createdOrders", createdOrders)
                .add("filledOrders", filledOrders)
                .add("canceledOrders", canceledOrders)
                .add("tradesClosed", tradesClosed)
                .add("tradesOpened", tradesOpened)
                .toString();
    }

    AccountChanges tradeOpened(OrderRequest orderRequest, TradeSummary filledPosition) {
        List<TradeSummary> newTradesOpened = new ArrayList<>(tradesOpened);
        newTradesOpened.add(filledPosition);

        List<MarketOrder> marketOrders = new ArrayList<>(filledOrders.getMarketOrders());
        List<LimitOrder> limitOrders = new ArrayList<>(filledOrders.getLimitOrders());

        if (orderRequest instanceof MarketOrderRequest) {
            marketOrders.add(new MarketOrder(filledPosition.getTradeId(), filledPosition.getAccountId(), filledPosition.getOpenTime(),
                    null, filledPosition.getOpenTime(), filledPosition.getInstrument(), filledPosition.getInitialUnits()));
        } else if (orderRequest instanceof LimitOrderRequest) {
            LimitOrderRequest request = (LimitOrderRequest) orderRequest;

            limitOrders.add(new LimitOrder(filledPosition.getTradeId(), filledPosition.getAccountId(), filledPosition.getOpenTime(),
                    null, filledPosition.getOpenTime(), filledPosition.getInstrument(), filledPosition.getInitialUnits(), request.getPrice()));
        } else {
            LOG.error("Unexpected order request type: {}", orderRequest.getClass().getName());
        }

        Orders newFilledOrders = new Orders(marketOrders, limitOrders,
                filledOrders.getTakeProfits(), filledOrders.getStopLosses());

        return new AccountChanges(createdOrders, newFilledOrders, canceledOrders, tradesClosed, newTradesOpened);
    }

    AccountChanges tradeClosed(TradeSummary filledPosition) {
        List<TradeSummary> newTradesClosed = new ArrayList<>(tradesClosed);
        newTradesClosed.add(filledPosition);

        return new AccountChanges(createdOrders, filledOrders, canceledOrders, newTradesClosed, tradesOpened);
    }

    static AccountChanges empty() {
        return new AccountChanges(Orders.empty(), Orders.empty(), Orders.empty(), emptyList(), emptyList());
    }
}
