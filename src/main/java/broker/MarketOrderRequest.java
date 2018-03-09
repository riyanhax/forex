package broker;

import market.Instrument;

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
}
