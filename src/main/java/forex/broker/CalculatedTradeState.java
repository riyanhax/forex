package forex.broker;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class CalculatedTradeState {

    private final String id;
    private final long unrealizedProfitLoss;

    public CalculatedTradeState(String id, long unrealizedProfitLoss) {
        this.id = id;
        this.unrealizedProfitLoss = unrealizedProfitLoss;
    }

    public String getId() {
        return id;
    }

    public long getUnrealizedProfitLoss() {
        return unrealizedProfitLoss;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalculatedTradeState that = (CalculatedTradeState) o;
        return unrealizedProfitLoss == that.unrealizedProfitLoss &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, unrealizedProfitLoss);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("unrealizedProfitLoss", unrealizedProfitLoss)
                .toString();
    }

    public static List<CalculatedTradeState> fromAll(List<TradeSummary> openTrades) {
        return openTrades.stream().map(CalculatedTradeState::from).collect(toList());
    }

    public static CalculatedTradeState from(TradeSummary openTrade) {
        Preconditions.checkArgument(openTrade.getRealizedProfitLoss() == 0L, "Should not calculate trade state for closed trades!");

        return new CalculatedTradeState(openTrade.getId(), openTrade.getUnrealizedProfitLoss());
    }
}
