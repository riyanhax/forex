package broker;

import com.google.common.base.MoreObjects;
import market.Instrument;

import java.util.Objects;

public class MarketOrderRequest {
    private Instrument instrument;
    private int units;
    private StopLossDetails stopLossOnFill;
    private TakeProfitDetails takeProfitOnFill;

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public void setStopLossOnFill(StopLossDetails stopLossOnFill) {
        this.stopLossOnFill = stopLossOnFill;
    }

    public void setTakeProfitOnFill(TakeProfitDetails takeProfitOnFill) {
        this.takeProfitOnFill = takeProfitOnFill;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public int getUnits() {
        return units;
    }

    public StopLossDetails getStopLossOnFill() {
        return stopLossOnFill;
    }

    public TakeProfitDetails getTakeProfitOnFill() {
        return takeProfitOnFill;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketOrderRequest that = (MarketOrderRequest) o;
        return units == that.units &&
                instrument == that.instrument &&
                Objects.equals(stopLossOnFill, that.stopLossOnFill) &&
                Objects.equals(takeProfitOnFill, that.takeProfitOnFill);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrument, units, stopLossOnFill, takeProfitOnFill);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("instrument", instrument)
                .add("units", units)
                .add("stopLossOnFill", stopLossOnFill)
                .add("takeProfitOnFill", takeProfitOnFill)
                .toString();
    }
}
