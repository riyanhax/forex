package broker;

import java.util.Comparator;

public class TradeSummary implements Comparable<TradeSummary> {
    private final String instrument;
    private final int currentUnits;
    private final double price;
    private final double realizedProfitLoss;
    private final double unrealizedProfitLoss;
    private final String openTime;
    private final String closeTime;
    private final String id;

    public TradeSummary(String instrument, int currentUnits, double price, double realizedProfitLoss,
                        double unrealizedProfitLoss, String openTime, String closeTime, String id) {
        this.instrument = instrument;
        this.currentUnits = currentUnits;
        this.price = price;
        this.realizedProfitLoss = realizedProfitLoss;
        this.unrealizedProfitLoss = unrealizedProfitLoss;
        this.openTime = openTime;
        this.closeTime = closeTime;
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

    public double getRealizedProfitLoss() {
        return realizedProfitLoss;
    }

    public double getUnrealizedPL() {
        return unrealizedProfitLoss;
    }

    public String getOpenTime() {
        return openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public String getId() {
        return id;
    }

    @Override
    public int compareTo(TradeSummary o) {
        return Comparator.comparing(TradeSummary::getOpenTime).compare(this, o);
    }
}
