package market.order;

import market.forex.Instrument;

import java.util.Optional;

public final class Orders {

    private static class MarketOrderImpl implements MarketOrder {
        private final Instrument instrument;
        private final int units;

        private MarketOrderImpl(Instrument instrument, int units) {
            this.instrument = instrument;
            this.units = units;
        }

        @Override
        public Instrument getInstrument() {
            return instrument;
        }

        @Override
        public int getUnits() {
            return units;
        }
    }

    private static class BuyMarketOrderImpl extends MarketOrderImpl implements BuyMarketOrder {
        private BuyMarketOrderImpl(Instrument instrument, int units) {
            super(instrument, units);
        }
    }

    private static class SellMarketOrderImpl<T extends Instrument> extends MarketOrderImpl implements SellMarketOrder {
        private SellMarketOrderImpl(T instrument, int units) {
            super(instrument, units);
        }
    }

    private Orders() {
    }

    public static BuyMarketOrder buyMarketOrder(final int shares, final Instrument instrument) {
        return new BuyMarketOrderImpl(instrument, shares);
    }

    public static SellMarketOrder sellMarketOrder(final int shares, final Instrument instrument) {
        return new SellMarketOrderImpl<>(instrument, shares);
    }
}
