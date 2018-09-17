package forex.market.order;

import forex.market.Instrument;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Optional;

public final class Orders {
    private static abstract class OrderImpl implements Order {
        private final Instrument instrument;
        private final int units;
        private final LocalDateTime expiration;

        private OrderImpl(Instrument instrument, int units, @Nullable LocalDateTime expiration) {
            this.instrument = instrument;
            this.units = units;
            this.expiration = expiration;
        }

        @Override
        public Instrument getInstrument() {
            return instrument;
        }

        @Override
        public int getUnits() {
            return units;
        }

        @Override
        public Optional<LocalDateTime> expiration() {
            return Optional.ofNullable(expiration);
        }
    }

    private static class MarketOrderImpl extends OrderImpl implements MarketOrder {
        private MarketOrderImpl(Instrument instrument, int units, LocalDateTime expiration) {
            super(instrument, units, expiration);
        }
    }

    private static class LimitOrderImpl extends OrderImpl implements LimitOrder {
        private final long limit;

        LimitOrderImpl(Instrument instrument, int units, long limit, LocalDateTime expiration) {
            super(instrument, units, expiration);

            this.limit = limit;
        }

        @Override
        public Optional<Long> limit() {
            return Optional.of(limit);
        }
    }

    private static class BuyMarketOrderImpl extends MarketOrderImpl implements BuyMarketOrder {
        private BuyMarketOrderImpl(Instrument instrument, int units, LocalDateTime expiration) {
            super(instrument, units, expiration);
        }
    }

    private static class SellMarketOrderImpl<T extends Instrument> extends MarketOrderImpl implements SellMarketOrder {
        private SellMarketOrderImpl(T instrument, int units, LocalDateTime expiration) {
            super(instrument, units, expiration);
        }
    }

    private static class BuyLimitOrderImpl extends LimitOrderImpl implements BuyLimitOrder {
        private BuyLimitOrderImpl(Instrument instrument, int units, long limit, LocalDateTime expiration) {
            super(instrument, units, limit, expiration);
        }
    }

    private static class SellLimitOrderImpl extends LimitOrderImpl implements SellLimitOrder {
        private SellLimitOrderImpl(Instrument instrument, int units, long limit, LocalDateTime expiration) {
            super(instrument, units, limit, expiration);
        }
    }

    private Orders() {
    }

    public static BuyMarketOrder buyMarketOrder(int shares, Instrument instrument) {
        return buyMarketOrder(shares, instrument, null);
    }

    public static BuyMarketOrder buyMarketOrder(int shares, Instrument instrument, LocalDateTime expiration) {
        return new BuyMarketOrderImpl(instrument, shares, expiration);
    }

    public static SellMarketOrder sellMarketOrder(int shares, Instrument instrument) {
        return sellMarketOrder(shares, instrument, null);
    }

    public static SellMarketOrder sellMarketOrder(int shares, Instrument instrument, LocalDateTime expiration) {
        return new SellMarketOrderImpl<>(instrument, shares, expiration);
    }

    public static BuyLimitOrder buyLimitOrder(int shares, Instrument instrument, long limit) {
        return buyLimitOrder(shares, instrument, limit, null);
    }

    public static BuyLimitOrder buyLimitOrder(int shares, Instrument instrument, long limit, LocalDateTime expiration) {
        return new BuyLimitOrderImpl(instrument, shares, limit, expiration);
    }

    public static SellLimitOrder sellLimitOrder(int shares, Instrument instrument, long limit) {
        return sellLimitOrder(shares, instrument, limit, null);
    }

    public static SellLimitOrder sellLimitOrder(int shares, Instrument instrument, long limit, LocalDateTime expiration) {
        return new SellLimitOrderImpl(instrument, shares, limit, expiration);
    }
}
