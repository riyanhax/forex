package forex.broker;

import com.google.common.base.MoreObjects.ToStringHelper;
import forex.market.Instrument;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "account_limit_order")
public class LimitOrder extends Order {

    @Column(nullable = false)
    private Instrument instrument;

    @Column(nullable = false)
    private int units;

    @Column(nullable = false)
    private long price;

    public LimitOrder() {
    }

    public LimitOrder(String orderId, String accountId, LocalDateTime createTime, LocalDateTime canceledTime, LocalDateTime filledTime,
                      Instrument instrument, int units, long price) {
        super(orderId, accountId, createTime, canceledTime, filledTime);

        this.instrument = instrument;
        this.units = units;
        this.price = price;
    }

    public LimitOrder(LimitOrderTransaction transaction) {
        this(transaction.getTransactionId(), transaction.getAccountId(), transaction.getTime(), null, null,
                transaction.getInstrument(), transaction.getUnits(), transaction.getPrice());
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

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LimitOrder that = (LimitOrder) o;
        return units == that.units &&
                price == that.price &&
                instrument == that.instrument;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), instrument, units, price);
    }

    @Override
    public ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("instrument", instrument)
                .add("units", units)
                .add("price", price);
    }
}
