package forex.broker;

import com.google.common.base.MoreObjects;
import forex.market.Instrument;

import java.time.LocalDateTime;
import java.util.Objects;

public class MarketOrderTransaction {

    // TODO: Use a MarketOrder reference instead?
    private final String orderId;
    private final String accountId;
    private final LocalDateTime createTime;
    private final Instrument instrument;
    private final int units;

    public MarketOrderTransaction(String orderId, String accountId, LocalDateTime createTime, Instrument instrument, int units) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.createTime = createTime;
        this.instrument = instrument;
        this.units = units;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getAccountId() {
        return accountId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public int getUnits() {
        return units;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketOrderTransaction that = (MarketOrderTransaction) o;
        return units == that.units &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(createTime, that.createTime) &&
                instrument == that.instrument;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, accountId, createTime, instrument, units);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("orderId", orderId)
                .add("accountId", accountId)
                .add("createTime", createTime)
                .add("instrument", instrument)
                .add("units", units)
                .toString();
    }
}
