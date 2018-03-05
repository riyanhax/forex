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
        private final long limit;

        public LimitOrderImpl(Instrument instrument, int units, long limit) {
            super(instrument, units);

            this.limit = limit;
        }

        @Override
        public Optional<Long> limit() {
            return Optional.of(limit);
        }
    }

    private static class StopOrderImpl extends OrderImpl implements StopOrder {
        private final double stop;

        private StopOrderImpl(Instrument instrument, int units, double stop) {
            super(instrument, units);

            this.stop = stop;
        }

        @Override
        public Optional<Double> stop() {
            return Optional.of(stop);
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
        private BuyLimitOrderImpl(Instrument instrument, int units, long limit) {
            super(instrument, units, limit);
        }
    }

    private static class SellLimitOrderImpl extends LimitOrderImpl implements SellLimitOrder {
        private SellLimitOrderImpl(Instrument instrument, int units, long limit) {
            super(instrument, units, limit);
        }
    }

    private static class SellStopOrderImpl extends StopOrderImpl implements SellStopOrder {
        private SellStopOrderImpl(Instrument instrument, int units, double stop) {
            super(instrument, units, stop);
        }
    }

    private static class BuyStopOrderImpl extends StopOrderImpl implements BuyStopOrder {
        private BuyStopOrderImpl(Instrument instrument, int units, double stop) {
            super(instrument, units, stop);
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

    public static BuyLimitOrder buyLimitOrder(int shares, Instrument instrument, long limit) {
        return new BuyLimitOrderImpl(instrument, shares, limit);
    }

    public static SellLimitOrder sellLimitOrder(int shares, Instrument instrument, long limit) {
        return new SellLimitOrderImpl(instrument, shares, limit);
    }

    public static BuyStopOrder buyStopOrder(int shares, Instrument instrument, double stop) {
        return new BuyStopOrderImpl(instrument, shares, stop);
    }

    public static SellStopOrder sellStopOrder(int shares, Instrument instrument, double stop) {
        return new SellStopOrderImpl(instrument, shares, stop);
    }
}
