package forex.broker;

public enum CandlePrice {
    BID("B"),
    MID("M"),
    ASK("A");

    private final String symbol;

    CandlePrice(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
