package trader;

import broker.OpenPositionRequest;
import com.google.common.collect.Range;
import market.Instrument;
import market.InstrumentHistoryService;
import market.MarketTime;
import market.OHLC;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

class SmarterRandomPosition implements TradingStrategy {

    private final Random random = new Random();

    @Override
    public Optional<OpenPositionRequest> shouldOpenPosition(MarketTime clock, InstrumentHistoryService instrumentHistoryService) throws Exception {
        LocalDateTime now = clock.now();
        if (!(now.getMinute() == 30)) {
            return Optional.empty();
        }

        Instrument[] instruments = Instrument.values();
        Instrument pair = instruments[random.nextInt(instruments.length)];

        NavigableMap<LocalDateTime, OHLC> oneWeekCandles = instrumentHistoryService.getOneDayCandles(pair, Range.closed(now.minusDays(10), now));

        NavigableMap<LocalDateTime, OHLC> oneWeekCandlesDescending = oneWeekCandles.descendingMap();
        Iterator<Map.Entry<LocalDateTime, OHLC>> oneWeekIter = oneWeekCandlesDescending.entrySet().iterator();

        double currentWeekHigh = oneWeekIter.next().getValue().close;
        double previousWeekHigh = oneWeekIter.next().getValue().close;
        double thirdWeekHigh = oneWeekIter.next().getValue().close;
        double fourthWeekHigh = oneWeekIter.next().getValue().close;
        double fifthWeekHigh = oneWeekIter.next().getValue().close;

        boolean checkingInverse = currentWeekHigh < previousWeekHigh;

        if ((currentWeekHigh > previousWeekHigh && previousWeekHigh > thirdWeekHigh && thirdWeekHigh < fourthWeekHigh && fourthWeekHigh < fifthWeekHigh)
                || (checkingInverse && previousWeekHigh < thirdWeekHigh && thirdWeekHigh > fourthWeekHigh && fourthWeekHigh > fifthWeekHigh)) {

            NavigableMap<LocalDateTime, OHLC> dayCandles = instrumentHistoryService.getFourHourCandles(pair, Range.closed(now.minusDays(7), now));
            NavigableMap<LocalDateTime, OHLC> newestToOldest = dayCandles.descendingMap();
            Set<Map.Entry<LocalDateTime, OHLC>> entries = newestToOldest.entrySet();
            Iterator<Map.Entry<LocalDateTime, OHLC>> iterator = entries.iterator();

            double currentHigh = iterator.next().getValue().close;
            double previousHigh = iterator.next().getValue().close;
            double thirdHigh = iterator.next().getValue().close;
            double fourthHigh = iterator.next().getValue().close;
            double fifthHigh = iterator.next().getValue().close;

            boolean openPosition = currentHigh > previousHigh && previousHigh > thirdHigh && thirdHigh < fourthHigh && fourthHigh < fifthHigh;
            if (openPosition) {
                return Optional.of(new OpenPositionRequest(pair, null, 30d, 60d));
            } else if (currentHigh < previousHigh && previousHigh < thirdHigh && thirdHigh > fourthHigh && fourthHigh > fifthHigh) {
                return Optional.of(new OpenPositionRequest(pair.getOpposite(), null, 30d, 60d));
            }
        }

        return Optional.empty();
    }
}
