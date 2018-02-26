package market.order;

import market.Instrument;

public final class Orders {

    private static class MarketOrderImpl<T extends Instrument> implements MarketOrder<T> {
        private final T instrument;
        private final int units;

        private MarketOrderImpl(T instrument, int units) {
            this.instrument = instrument;
            this.units = units;
        }

        @Override
        public T getInstrument() {
            return instrument;
        }

        @Override
        public int getUnits() {
            return units;
        }
    }

    private static class BuyMarketOrderImpl<T extends Instrument> extends MarketOrderImpl<T> implements BuyMarketOrder<T> {
        private BuyMarketOrderImpl(T instrument, int units) {
            super(instrument, units);
        }
    }

    private static class SellMarketOrderImpl<T extends Instrument> extends MarketOrderImpl<T> implements SellMarketOrder<T> {
        private SellMarketOrderImpl(T instrument, int units) {
            super(instrument, units);
        }
    }

    private Orders() {
    }

    public static <T extends Instrument> BuyMarketOrder<T> buyMarketOrder(final int shares, final T instrument) {
        return new BuyMarketOrderImpl<>(instrument, shares);
    }

    public static <T extends Instrument> SellMarketOrder<T> sellMarketOrder(final int shares, final T instrument) {
        return new SellMarketOrderImpl<>(instrument, shares);
    }
}
