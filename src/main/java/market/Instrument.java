package market;

public enum Instrument {
    EURUSD("EURUSD", "EUR/USD", false) {
        @Override
        public Instrument getOpposite() {
            return USDEUR;
        }
    }, USDEUR("USDEUR", "USD/EUR", true) {
        @Override
        public Instrument getOpposite() {
            return EURUSD;
        }
    };

    private final String symbol;
    private final String name;
    private final boolean inverse;

    Instrument(String symbol, String name, boolean inverse) {
        this.symbol = symbol;
        this.name = name;
        this.inverse = inverse;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public boolean isInverse() {
        return inverse;
    }

    public double getPip() {
        return 0.0001d;
    }

    public abstract Instrument getOpposite();
}
