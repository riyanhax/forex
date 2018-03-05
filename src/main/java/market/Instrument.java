package market;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;

public enum Instrument {
    EURUSD("EUR_USD", "EUR/USD", false) {
        @Override
        public Instrument getOpposite() {
            return USDEUR;
        }
    }, USDEUR("USD_EUR", "USD/EUR", true) {
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

    public static final Map<String, Instrument> bySymbol = Maps.uniqueIndex(Arrays.asList(Instrument.values()),
            Instrument::getSymbol);

    public Instrument getBrokerInstrument() {
        return inverse ? getOpposite() : this;
    }
}
