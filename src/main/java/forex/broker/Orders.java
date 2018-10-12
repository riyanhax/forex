package forex.broker;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

public class Orders {
    private final List<MarketOrder> marketOrders;
    private final List<LimitOrder> limitOrders;
    private final List<TakeProfitOrder> takeProfits;
    private final List<StopLossOrder> stopLosses;

    public Orders(Collection<MarketOrder> marketOrders, Collection<LimitOrder> limitOrders, Collection<TakeProfitOrder> takeProfits, Collection<StopLossOrder> stopLosses) {
        this.marketOrders = new ArrayList<>(marketOrders);
        this.limitOrders = new ArrayList<>(limitOrders);
        this.takeProfits = new ArrayList<>(takeProfits);
        this.stopLosses = new ArrayList<>(stopLosses);
    }

    public List<MarketOrder> getMarketOrders() {
        return marketOrders;
    }

    public List<LimitOrder> getLimitOrders() {
        return limitOrders;
    }

    public List<TakeProfitOrder> getTakeProfits() {
        return takeProfits;
    }

    public List<StopLossOrder> getStopLosses() {
        return stopLosses;
    }

    public List<Order> all() {
        List<Order> all = new ArrayList<>(marketOrders);
        all.addAll(limitOrders);
        all.addAll(takeProfits);
        all.addAll(stopLosses);

        return all;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Orders orders = (Orders) o;
        return Objects.equals(marketOrders, orders.marketOrders) &&
                Objects.equals(limitOrders, orders.limitOrders) &&
                Objects.equals(takeProfits, orders.takeProfits) &&
                Objects.equals(stopLosses, orders.stopLosses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(marketOrders, limitOrders, takeProfits, stopLosses);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("marketOrders", marketOrders)
                .add("limitOrders", limitOrders)
                .add("takeProfits", takeProfits)
                .add("stopLosses", stopLosses)
                .toString();
    }

    public static Orders empty() {
        return new Orders(emptyList(), emptyList(), emptyList(), emptyList());
    }

}
