package broker;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class AccountChangesState {

    private final long netAssetValue;
    private final long unrealizedProfitAndLoss;

    public AccountChangesState(long netAssetValue, long unrealizedProfitAndLoss) {
        this.netAssetValue = netAssetValue;
        this.unrealizedProfitAndLoss = unrealizedProfitAndLoss;
    }

    public long getNetAssetValue() {
        return netAssetValue;
    }

    public long getUnrealizedProfitAndLoss() {
        return unrealizedProfitAndLoss;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountChangesState that = (AccountChangesState) o;
        return netAssetValue == that.netAssetValue &&
                unrealizedProfitAndLoss == that.unrealizedProfitAndLoss;
    }

    @Override
    public int hashCode() {
        return Objects.hash(netAssetValue, unrealizedProfitAndLoss);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("netAssetValue", netAssetValue)
                .add("unrealizedProfitAndLoss", unrealizedProfitAndLoss)
                .toString();
    }
}
