package broker;

import java.text.DecimalFormat;

public interface Quote {

    DecimalFormat dollarFormatter = new DecimalFormat("$####,###,##0.00000");

    /**
     * Used to convert from longs to a currency quote (e.g. 1.23456 is stored as 123456)
     */
    long PIPPETE_CONVERSION = 100000;

    long getBid();

    long getAsk();

    static long pippetesFromDouble(boolean inverse, double value) {
        return inverse ? pippetesFromDouble(1d / value) : pippetesFromDouble(value);
    }

    static long pippetesFromDouble(double value) {
        return Math.round(value * PIPPETE_CONVERSION);
    }

    static String formatDollars(long pippetes) {
        return dollarFormatter.format(doubleFromPippetes(pippetes));
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

        return String.format("%s.%s pips", pips, Math.abs(remainder));
    }

    static String profitLossDisplay(long pipettes) {
        String dollars = formatDollars(pipettes);

        return String.format("%s, %s, (%d pipettes)", dollars, pipsFromPippetes(pipettes), pipettes);
    }
}
