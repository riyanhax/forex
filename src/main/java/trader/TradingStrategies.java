package trader;

import broker.CandlestickData;
import broker.ForexBroker;
import broker.OpenPositionRequest;
import com.google.common.collect.Range;
import market.Instrument;
import market.MarketTime;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public enum TradingStrategies implements TradingStrategy {

    OPEN_RANDOM_POSITION {
        @Override
        public Optional<OpenPositionRequest> shouldOpenPosition(ForexTrader trader, ForexBroker broker, MarketTime clock) {
            Instrument pair = TradingStrategies.randomInstrument();
            return Optional.of(new OpenPositionRequest(pair, null, 300L, 600L));
        }
    },
    OPEN_RANDOM_POSITION_HIGH_FREQUENCY {
        @Override
        public Optional<OpenPositionRequest> shouldOpenPosition(ForexTrader trader, ForexBroker broker, MarketTime clock) {
            Instrument pair = TradingStrategies.randomInstrument();
            return Optional.of(new OpenPositionRequest(pair, null, 100L, 100L));
        }
    },
    SMARTER_RANDOM_POSITION {
        @Override
        public Optional<OpenPositionRequest> shouldOpenPosition(ForexTrader trader, ForexBroker broker, MarketTime clock) throws Exception {
            LocalDateTime now = clock.now();
            if (!(now.getMinute() == 30)) {
                return Optional.empty();
            }

            Instrument pair = randomInstrument();
            NavigableMap<LocalDateTime, CandlestickData> oneWeekCandles = broker.getOneDayCandles(trader, pair, Range.closed(now.minusDays(10), now));

            NavigableMap<LocalDateTime, CandlestickData> oneWeekCandlesDescending = oneWeekCandles.descendingMap();
            Iterator<Map.Entry<LocalDateTime, CandlestickData>> oneWeekIter = oneWeekCandlesDescending.entrySet().iterator();

            long currentWeekHigh = oneWeekIter.next().getValue().getC();
            long previousWeekHigh = oneWeekIter.next().getValue().getC();
            long thirdWeekHigh = oneWeekIter.next().getValue().getC();
            long fourthWeekHigh = oneWeekIter.next().getValue().getC();
            long fifthWeekHigh = oneWeekIter.next().getValue().getC();

            boolean checkingInverse = currentWeekHigh < previousWeekHigh;

            if ((currentWeekHigh > previousWeekHigh && previousWeekHigh > thirdWeekHigh && thirdWeekHigh < fourthWeekHigh && fourthWeekHigh < fifthWeekHigh)
                    || (checkingInverse && previousWeekHigh < thirdWeekHigh && thirdWeekHigh > fourthWeekHigh && fourthWeekHigh > fifthWeekHigh)) {

                NavigableMap<LocalDateTime, CandlestickData> dayCandles = broker.getFourHourCandles(trader, pair, Range.closed(now.minusDays(7), now));
                NavigableMap<LocalDateTime, CandlestickData> newestToOldest = dayCandles.descendingMap();
                Set<Map.Entry<LocalDateTime, CandlestickData>> entries = newestToOldest.entrySet();
                Iterator<Map.Entry<LocalDateTime, CandlestickData>> iterator = entries.iterator();

                long currentHigh = iterator.next().getValue().getC();
                long previousHigh = iterator.next().getValue().getC();
                long thirdHigh = iterator.next().getValue().getC();
                long fourthHigh = iterator.next().getValue().getC();
                long fifthHigh = iterator.next().getValue().getC();

                boolean openPosition = currentHigh > previousHigh && previousHigh > thirdHigh && thirdHigh < fourthHigh && fourthHigh < fifthHigh;
                if (openPosition) {
                    return Optional.of(new OpenPositionRequest(pair, null, 300L, 600L));
                } else if (currentHigh < previousHigh && previousHigh < thirdHigh && thirdHigh > fourthHigh && fourthHigh > fifthHigh) {
                    return Optional.of(new OpenPositionRequest(pair.getOpposite(), null, 300L, 600L));
                }
            }

            return Optional.empty();
        }
    };

    private static final Random random = new Random();

    private static Instrument randomInstrument() {
        Instrument[] instruments = Instrument.values();
        return instruments[random.nextInt(instruments.length)];
    }
}
