package market.forex;

import market.Instrument;

public enum CurrencyPair implements Instrument {
    EURUSD("EURUSD", "EUR/USD");

    private final String symbol;
    private final String name;

    CurrencyPair(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public String getName() {
        return name;
    }

    public double getPip() {
        return 0.0001d;
    }
}
