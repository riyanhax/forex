package forex.broker;

import com.google.common.base.MoreObjects.ToStringHelper;
import forex.market.Instrument;

import java.time.LocalDateTime;
import java.util.Objects;

public class MarketOrder extends Order {

    private final Instrument instrument;
    private final int units;

    public MarketOrder(String orderId, LocalDateTime createTime, LocalDateTime canceledTime, LocalDateTime filledTime,
                       Instrument instrument, int units) {
        super(orderId, createTime, canceledTime, filledTime);

        this.instrument = instrument;
        this.units = units;
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
