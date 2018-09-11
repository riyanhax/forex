package broker;

import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.Objects;

public class AccountChangesState {

    private final long netAssetValue;
    private final long unrealizedProfitAndLoss;
    private final List<CalculatedTradeState> trades;

    public AccountChangesState(long netAssetValue, long unrealizedProfitAndLoss, List<CalculatedTradeState> trades) {
        this.netAssetValue = netAssetValue;
        this.unrealizedProfitAndLoss = unrealizedProfitAndLoss;
        this.trades = trades;
    }

    public long getNetAssetValue() {
        return netAssetValue;
    }

    public long getUnrealizedProfitAndLoss() {
        return unrealizedProfitAndLoss;
    }

    public List<CalculatedTradeState> getTrades() {
        return trades;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountChangesState that = (AccountChangesState) o;
        return netAssetValue == that.netAssetValue &&
                unrealizedProfitAndLoss == that.unrealizedProfitAndLoss &&
                Objects.equals(trades, that.trades);
    }

    @Override
    public int hashCode() {
        return Objects.hash(netAssetValue, unrealizedProfitAndLoss, trades);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("netAssetValue", netAssetValue)
                .add("unrealizedProfitAndLoss", unrealizedProfitAndLoss)
                .add("trades", trades)
                .toString();
    }
}
