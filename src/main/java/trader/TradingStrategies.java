package trader;

import broker.CandlestickData;
import broker.OpenPositionRequest;
import com.google.common.collect.Range;
import market.Instrument;
import market.InstrumentHistoryService;
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
        public Optional<OpenPositionRequest> shouldOpenPosition(MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
            Instrument pair = TradingStrategies.randomInstrument();
            return Optional.of(new OpenPositionRequest(pair, null, 300L, 600L));
        }
    },
    OPEN_RANDOM_POSITION_HIGH_FREQUENCY {
        @Override
        public Optional<OpenPositionRequest> shouldOpenPosition(MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
            Instrument pair = TradingStrategies.randomInstrument();
            return Optional.of(new OpenPositionRequest(pair, null, 100L, 100L));
        }
    },
    SMARTER_RANDOM_POSITION {
        @Override
        public Optional<OpenPositionRequest> shouldOpenPosition(MarketTime clock, InstrumentHistoryService instrumentHistoryService) throws Exception {
            LocalDateTime now = clock.now();
            if (!(now.getMinute() == 30)) {
                return Optional.empty();
            }

            Instrument pair = randomInstrument();
            NavigableMap<LocalDateTime, CandlestickData> oneWeekCandles = instrumentHistoryService.getOneDayCandles(pair, Range.closed(now.minusDays(10), now));

            NavigableMap<LocalDateTime, CandlestickData> oneWeekCandlesDescending = oneWeekCandles.descendingMap();
            Iterator<Map.Entry<LocalDateTime, CandlestickData>> oneWeekIter = oneWeekCandlesDescending.entrySet().iterator();

            double currentWeekHigh = oneWeekIter.next().getValue().getC();
            double previousWeekHigh = oneWeekIter.next().getValue().getC();
            double thirdWeekHigh = oneWeekIter.next().getValue().getC();
            double fourthWeekHigh = oneWeekIter.next().getValue().getC();
            double fifthWeekHigh = oneWeekIter.next().getValue().getC();

            boolean checkingInverse = currentWeekHigh < previousWeekHigh;

            if ((currentWeekHigh > previousWeekHigh && previousWeekHigh > thirdWeekHigh && thirdWeekHigh < fourthWeekHigh && fourthWeekHigh < fifthWeekHigh)
                    || (checkingInverse && previousWeekHigh < thirdWeekHigh && thirdWeekHigh > fourthWeekHigh && fourthWeekHigh > fifthWeekHigh)) {

                NavigableMap<LocalDateTime, CandlestickData> dayCandles = instrumentHistoryService.getFourHourCandles(pair, Range.closed(now.minusDays(7), now));
                NavigableMap<LocalDateTime, CandlestickData> newestToOldest = dayCandles.descendingMap();
                Set<Map.Entry<LocalDateTime, CandlestickData>> entries = newestToOldest.entrySet();
                Iterator<Map.Entry<LocalDateTime, CandlestickData>> iterator = entries.iterator();

                double currentHigh = iterator.next().getValue().getC();
                double previousHigh = iterator.next().getValue().getC();
                double thirdHigh = iterator.next().getValue().getC();
                double fourthHigh = iterator.next().getValue().getC();
                double fifthHigh = iterator.next().getValue().getC();

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
