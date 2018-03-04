package market.order;

import market.Instrument;

import java.util.Optional;

public final class Orders {
    private static abstract class OrderImpl implements Order {
        private final Instrument instrument;
        private final int units;

        private OrderImpl(Instrument instrument, int units) {
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

    private static class MarketOrderImpl extends OrderImpl implements MarketOrder {
        private MarketOrderImpl(Instrument instrument, int units) {
            super(instrument, units);
        }
    }

    private static class LimitOrderImpl extends OrderImpl implements LimitOrder {
        private final double limit;

        public LimitOrderImpl(Instrument instrument, int units, double limit) {
            super(instrument, units);

            this.limit = limit;
        }

        @Override
        public Optional<Double> limit() {
            return Optional.of(limit);
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

    private static class BuyLimitOrderImpl extends LimitOrderImpl implements BuyLimitOrder {
        private BuyLimitOrderImpl(Instrument instrument, int units, double limit) {
            super(instrument, units, limit);
        }
    }

    private static class SellLimitOrderImpl extends LimitOrderImpl implements SellLimitOrder {
        private SellLimitOrderImpl(Instrument instrument, int units, double limit) {
            super(instrument, units, limit);
        }
    }

    private Orders() {
    }

    public static BuyMarketOrder buyMarketOrder(int shares, Instrument instrument) {
        return new BuyMarketOrderImpl(instrument, shares);
    }

    public static SellMarketOrder sellMarketOrder(int shares, Instrument instrument) {
        return new SellMarketOrderImpl<>(instrument, shares);
    }

    public static BuyLimitOrder buyLimitOrder(int shares, Instrument instrument, double limit) {
        return new BuyLimitOrderImpl(instrument, shares, limit);
    }

    public static SellLimitOrder sellLimitOrder(int shares, Instrument instrument, double limit) {
        return new SellLimitOrderImpl(instrument, shares, limit);
    }
}
