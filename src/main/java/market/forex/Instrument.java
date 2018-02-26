package market.forex;

public enum Instrument {
    EURUSD("EURUSD", "EUR/USD");

    private final String symbol;
    private final String name;

    Instrument(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public double getPip() {
        return 0.0001d;
    }
}
