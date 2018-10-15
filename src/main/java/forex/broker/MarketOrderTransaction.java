package forex.broker;

import com.google.common.base.MoreObjects.ToStringHelper;
import forex.market.Instrument;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "account_transaction_market_order")
public class MarketOrderTransaction extends Transaction implements OrderTransaction {

    @Column(nullable = false)
    private Instrument instrument;

    @Column(nullable = false)
    private int units;

    public MarketOrderTransaction() {
    }

    public MarketOrderTransaction(String orderId, String accountId, LocalDateTime createTime, Instrument instrument, int units) {
        super(orderId, accountId, createTime);

        this.instrument = instrument;
        this.units = units;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MarketOrderTransaction that = (MarketOrderTransaction) o;
        return units == that.units &&
                instrument == that.instrument;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), instrument, units);
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("instrument", instrument)
                .add("units", units);
    }
}
