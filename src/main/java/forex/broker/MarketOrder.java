package forex.broker;

import com.google.common.base.MoreObjects.ToStringHelper;
import forex.market.Instrument;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "account_market_order")
public class MarketOrder extends Order {

    @Column(nullable = false)
    private Instrument instrument;

    @Column(nullable = false)
    private int units;

    public MarketOrder() {
    }

    public MarketOrder(MarketOrderTransaction transaction) {
        this(transaction.getTransactionId(), transaction.getAccountId(), transaction.getTime(), null,
                null, transaction.getInstrument(), transaction.getUnits());
    }

    public MarketOrder(String orderId, String accountId, LocalDateTime createTime, LocalDateTime canceledTime, LocalDateTime filledTime,
                       Instrument instrument, int units) {
        super(orderId, accountId, createTime, canceledTime, filledTime);

        this.instrument = instrument;
        this.units = units;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MarketOrder that = (MarketOrder) o;
        return units == that.units &&
                instrument == that.instrument;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), instrument, units);
    }

    @Override
    public ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("instrument", instrument)
                .add("units", units);
    }
}
