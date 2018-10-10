package forex.broker;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

public class Orders {
    private final List<MarketOrder> marketOrders;
    private final List<TakeProfitOrder> takeProfits;
    private final List<StopLossOrder> stopLosses;

    public Orders(List<MarketOrder> marketOrders, List<TakeProfitOrder> takeProfits, List<StopLossOrder> stopLosses) {
        this.marketOrders = marketOrders;
        this.takeProfits = takeProfits;
        this.stopLosses = stopLosses;
    }

    public List<MarketOrder> getMarketOrders() {
        return marketOrders;
    }

    public List<TakeProfitOrder> getTakeProfits() {
        return takeProfits;
    }

    public List<StopLossOrder> getStopLosses() {
        return stopLosses;
    }

    public List<Order> all() {
        List<Order> all = new ArrayList<>(marketOrders);
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
                Objects.equals(takeProfits, orders.takeProfits) &&
                Objects.equals(stopLosses, orders.stopLosses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(takeProfits, stopLosses);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("marketOrders", marketOrders)
                .add("takeProfits", takeProfits)
                .add("stopLosses", stopLosses)
                .toString();
    }

    public static Orders empty() {
        return new Orders(emptyList(), emptyList(), emptyList());
    }

}
