package broker;

public interface Quote {

    /**
     * Used to convert from longs to a currency quote (e.g. 1.23456 is stored as 123456)
     */
    long PIPPETE_CONVERSION = 100000;

    long getBid();

    long getAsk();

    static long pippetesFromDouble(double value) {
        return Math.round(value * PIPPETE_CONVERSION);
    }

    static double doubleFromPippetes(long value) {
        return value / (double) PIPPETE_CONVERSION;
    }

    static long invert(long price) {
        return PIPPETE_CONVERSION * PIPPETE_CONVERSION / price;
    }

    static String pipsFromPippetes(long pipettes) {
        long pips = pipettes / 10;
        long remainder = pipettes % 10;

        return String.format("%s.%s", pips, Math.abs(remainder));
    }
}
