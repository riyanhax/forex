package forex.broker;

import com.google.common.base.MoreObjects.ToStringHelper;
import forex.market.Instrument;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "account_transaction_limit_order")
public class LimitOrderTransaction extends Transaction implements OrderTransaction {

    @Column(nullable = false)
    private Instrument instrument;

    @Column(nullable = false)
    private int units;

    @Column(nullable = false)
    private long price;

    public LimitOrderTransaction(String orderId, String accountId, LocalDateTime createTime,
                                 Instrument instrument, int units, long price) {
        super(orderId, accountId, createTime);

        this.instrument = instrument;
        this.units = units;
        this.price = price;
    }

    @Override
    public Instrument getInstrument() {
        return instrument;
    }

    @Override
    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    @Override
    public int getUnits() {
        return units;
    }

    @Override
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
        LimitOrderTransaction that = (LimitOrderTransaction) o;
        return units == that.units &&
                price == that.price &&
                instrument == that.instrument;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), instrument, units, price);
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("instrument", instrument)
                .add("units", units)
                .add("price", price);
    }
}
