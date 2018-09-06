package trader;

import broker.CandlestickData;
import broker.ForexBroker;
import broker.OpenPositionRequest;
import broker.RequestException;
import broker.TradeSummary;
import com.google.common.collect.Range;
import market.Instrument;
import market.MarketTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Random;

public enum TradingStrategies implements TradingStrategy {

    HIGH_FREQ_MARTINGALE {
        @Override
        public Optional<OpenPositionRequest> shouldOpenPosition(ForexTrader trader, ForexBroker broker, MarketTime clock) throws Exception {
            Optional<OpenPositionRequest> openPositionRequest = OPEN_RANDOM_POSITION_HIGH_FREQUENCY.shouldOpenPosition(trader, broker, clock);
            if (openPositionRequest.isPresent()) {
                return martingaleRequest(openPositionRequest.get(), trader);
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
    SMARTER_MARTINGALE {
        @Override
        public Optional<OpenPositionRequest> shouldOpenPosition(ForexTrader trader, ForexBroker broker, MarketTime clock) throws Exception {
            LocalDateTime now = clock.now();
            if (!(now.getMinute() % 16 == 0)) {
                return Optional.empty();
            }

            Optional<OpenPositionRequest> openPositionRequest = SMARTER_RANDOM_POSITION.shouldOpenPosition(trader, broker, clock);
            if (openPositionRequest.isPresent()) {
                return martingaleRequest(openPositionRequest.get(), trader);
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

    private static Optional<OpenPositionRequest> martingaleRequest(OpenPositionRequest toCopy, ForexTrader trader) throws RequestException {
        int units = 1;
        Optional<TradeSummary> lastClosedTrade = trader.getLastClosedTrade();
        if (lastClosedTrade.isPresent()) {
            TradeSummary trade = lastClosedTrade.get();


            if (trade.getRealizedProfitLoss() < 0) {
                units = trade.getCurrentUnits() * 2;

                LOG.info("Using {} units due to unprofitable trade: {}", units, trade);
            }
        }

        return Optional.of(new OpenPositionRequest(toCopy.getPair(), units, toCopy.getLimit().orElse(null),
                toCopy.getStopLoss().orElse(null), toCopy.getTakeProfit().orElse(null)));
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
