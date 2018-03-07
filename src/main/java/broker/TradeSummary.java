package broker;

public class TradeSummary {
    private final String instrument;
    private final int currentUnits;
    private final double price;
    private final double unrealizedProfitLoss;
    private final String openTime;
    private final String id;

    public TradeSummary(String instrument, int currentUnits, double price, double unrealizedProfitLoss, String openTime, String id) {
        this.instrument = instrument;
        this.currentUnits = currentUnits;
        this.price = price;
        this.unrealizedProfitLoss = unrealizedProfitLoss;
        this.openTime = openTime;
        this.id = id;
    }

    public String getInstrument() {
        return instrument;
    }

    public int getCurrentUnits() {
        return this.currentUnits;
    }

    public double getPrice() {
        return price;
    }

    public double getUnrealizedPL() {
        return unrealizedProfitLoss;
    }

    public String getOpenTime() {
        return openTime;
    }

    public String getId() {
        return id;
    }
}
