package forex.trader;

import com.google.common.collect.Range;
import forex.broker.CandlestickData;
import forex.broker.ForexBroker;
import forex.broker.OpenPositionRequest;
import forex.broker.RequestException;
import forex.broker.TradeSummary;
import forex.market.Instrument;
import forex.market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Random;

public enum TradingStrategies implements TradingStrategy {

    HIGH_FREQ_MARTINGALE_FIVEK_START {
        @Override
        public Optional<OpenPositionRequest> shouldOpenPosition(ForexTrader trader, ForexBroker broker, MarketTime clock) throws Exception {
            Optional<OpenPositionRequest> openPositionRequest = OPEN_RANDOM_POSITION_HIGH_FREQUENCY.shouldOpenPosition(trader, broker, clock);
            if (openPositionRequest.isPresent()) {
                return martingaleRequest(openPositionRequest.get(), trader, 100);
            }
            return openPositionRequest;
        }
    }, HIGH_FREQ_MARTINGALE {
        @Override
        public Optional<OpenPositionRequest> shouldOpenPosition(ForexTrader trader, ForexBroker broker, MarketTime clock) throws Exception {
            Optional<OpenPositionRequest> openPositionRequest = OPEN_RANDOM_POSITION_HIGH_FREQUENCY.shouldOpenPosition(trader, broker, clock);
            if (openPositionRequest.isPresent()) {
                return martingaleRequest(openPositionRequest.get(), trader, 1);
            }
            return openPositionRequest;
        }
    }, HISTORY_COMPARATOR2 {
        @Override
        public Optional<OpenPositionRequest> shouldOpenPosition(ForexTrader trader, ForexBroker broker, MarketTime clock) throws Exception {
            LocalDateTime now = clock.now();
            int minute = now.getMinute();

            if (!(minute % 16 == 0)) {
                return Optional.empty();
            }

            Instrument instrument = minute % 32 == 0 ? Instrument.USDEUR : Instrument.EURUSD;
            int units = (minute % 3) + 1;
            return Optional.of(new OpenPositionRequest(instrument, units, null, 300L, 600L));
        }
    },
    /**
     * Checks for the one week, one day, and four hour candles to all be higher than previous highs or
     * not higher than previous highs. If the candles all match, then it opens positions in martingale
     * fashion for the purpose of regression testing various position sizes and position stances.
     */
    REGRESSION_COMPARATOR {
        @Override
        public Optional<OpenPositionRequest> shouldOpenPosition(ForexTrader trader, ForexBroker broker, MarketTime clock) throws Exception {
            LocalDateTime now = clock.now();
            if (!(now.getMinute() % 10 == 0)) {
                return Optional.empty();
            }

            Instrument pair = Instrument.EURUSD;

            CandlestickData[] oneWeekCandles = TradingStrategies.twoMostRecent(broker.getOneWeekCandles(trader, pair, Range.closed(now.minusWeeks(3), now)));
            CandlestickData thisWeek = oneWeekCandles[0];
            CandlestickData lastWeek = oneWeekCandles[1];

            boolean thisWeekHigher = thisWeek.getH() > lastWeek.getH();

            CandlestickData[] oneDayCandles = TradingStrategies.twoMostRecent(broker.getOneDayCandles(trader, pair, Range.closed(now.minusDays(5), now)));
            CandlestickData today = oneDayCandles[0];
            CandlestickData yesterday = oneDayCandles[1];

            boolean todayHigher = today.getH() > yesterday.getH();

            if (!(thisWeekHigher == todayHigher)) {
                return Optional.empty();
            }

            CandlestickData[] fourHourCandles = TradingStrategies.twoMostRecent(broker.getFourHourCandles(trader, pair, Range.closed(now.minusDays(5), now)));
            long currentHigh = fourHourCandles[0].getH();
            long previousHigh = fourHourCandles[1].getH();

            boolean thisFourHigher = currentHigh > previousHigh;

            if (!(todayHigher == thisFourHigher)) {
                return Optional.empty();
            }

            Instrument instrument = todayHigher ? pair : pair.getOpposite();
            int units = martingaleUnits(trader, 1);

            return martingaleRequest(new OpenPositionRequest(instrument, units, null, 1000L, 2000L), trader, 1);
        }
    },
    SMARTER_MARTINGALE {
        @Override
        public Optional<OpenPositionRequest> shouldOpenPosition(ForexTrader trader, ForexBroker broker, MarketTime clock) throws Exception {
            LocalDateTime now = clock.now();
            if (!(now.getMinute() % 16 == 0)) {
                return Optional.empty();
            }

            Optional<OpenPositionRequest> openPositionRequest = SMARTER_RANDOM_POSITION.shouldOpenPosition(trader, broker, clock);
            if (openPositionRequest.isPresent()) {
                return martingaleRequest(openPositionRequest.get(), trader, 1);
            }

            return openPositionRequest;
        }
    },
    OPEN_RANDOM_POSITION {
        @Override
        public Optional<OpenPositionRequest> shouldOpenPosition(ForexTrader trader, ForexBroker broker, MarketTime clock) {
            Instrument pair = TradingStrategies.randomInstrument();
            return Optional.of(new OpenPositionRequest(pair, 1, null, 300L, 600L));
        }
    },
    OPEN_RANDOM_POSITION_HIGH_FREQUENCY {
        @Override
        public Optional<OpenPositionRequest> shouldOpenPosition(ForexTrader trader, ForexBroker broker, MarketTime clock) {
            Instrument pair = TradingStrategies.randomInstrument();
            return Optional.of(new OpenPositionRequest(pair, 1, null, 100L, 100L));
        }
    },
    SMARTER_RANDOM_POSITION {
        @Override
        public Optional<OpenPositionRequest> shouldOpenPosition(ForexTrader trader, ForexBroker broker, MarketTime clock) throws Exception {
            LocalDateTime now = clock.now();
            if (!(now.getMinute() % 16 == 0)) {
                return Optional.empty();
            }

            Instrument pair = randomInstrument();

            CandlestickData[] oneWeekCandles = TradingStrategies.twoMostRecent(broker.getOneWeekCandles(trader, pair, Range.closed(now.minusWeeks(3), now)));
            CandlestickData thisWeek = oneWeekCandles[0];
            CandlestickData lastWeek = oneWeekCandles[1];

            boolean thisWeekHigher = thisWeek.getH() > lastWeek.getH();

            CandlestickData[] oneDayCandles = TradingStrategies.twoMostRecent(broker.getOneDayCandles(trader, pair, Range.closed(now.minusDays(5), now)));
            CandlestickData today = oneDayCandles[0];
            CandlestickData yesterday = oneDayCandles[1];

            boolean todayHigher = today.getH() > yesterday.getH();

            if (!(thisWeekHigher == todayHigher)) {
                return Optional.empty();
            }

            CandlestickData[] fourHourCandles = TradingStrategies.twoMostRecent(broker.getFourHourCandles(trader, pair, Range.closed(now.minusDays(5), now)));
            long currentHigh = fourHourCandles[0].getH();
            long previousHigh = fourHourCandles[1].getH();

            boolean thisFourHigher = currentHigh > previousHigh;

            if (!(todayHigher == thisFourHigher)) {
                return Optional.empty();
            }

            if (todayHigher) {
                return Optional.of(new OpenPositionRequest(pair, 1, null, 1000L, 2000L));
            } else {
                return Optional.of(new OpenPositionRequest(pair.getOpposite(), 1, null, 1000L, 2000L));
            }

        }
    };

    @Override
    public String getName() {
        return name();
    }

    private static final Logger LOG = LoggerFactory.getLogger(TradingStrategies.class);

    private static final Random random = new Random();

    private static Optional<OpenPositionRequest> martingaleRequest(OpenPositionRequest toCopy, ForexTrader trader, int baseUnits) throws RequestException {
        int units = martingaleUnits(trader, baseUnits);
        return Optional.of(new OpenPositionRequest(toCopy.getPair(), units, toCopy.getLimit().orElse(null),
                toCopy.getStopLoss().orElse(null), toCopy.getTakeProfit().orElse(null)));
    }

    /**
     * Determines the martingale units to purchase in the next trade based on the last trade.
     */
    private static int martingaleUnits(ForexTrader trader, int baseUnits) throws RequestException {
        int units = baseUnits;
        Optional<TradeSummary> lastClosedTrade = trader.getLastClosedTrade();
        if (lastClosedTrade.isPresent()) {
            TradeSummary trade = lastClosedTrade.get();

            if (trade.getRealizedProfitLoss() < 0) {
                units = trade.getInitialUnits() * 2;

                LOG.info("Using {} units due to unprofitable trade: {}", units, trade);
            }
        }

        return units;
    }

    private static Instrument randomInstrument() {
        Instrument[] instruments = Instrument.values();
        return instruments[random.nextInt(instruments.length)];
    }

    private static CandlestickData[] twoMostRecent(NavigableMap<LocalDateTime, CandlestickData> ascendingCandles) {
        NavigableMap<LocalDateTime, CandlestickData> descending = ascendingCandles.descendingMap();
        Iterator<Map.Entry<LocalDateTime, CandlestickData>> oneWeekIter = descending.entrySet().iterator();

        Map.Entry<LocalDateTime, CandlestickData> mostRecent = oneWeekIter.next();
        Map.Entry<LocalDateTime, CandlestickData> second = oneWeekIter.next();

        return new CandlestickData[]{mostRecent.getValue(), second.getValue()};
    }
}
