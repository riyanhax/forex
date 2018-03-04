package trader;

import com.google.common.collect.Range;
import market.CandleTimeFrame;
import market.InstrumentHistoryService;
import market.OHLC;
import market.Instrument;
import market.MarketTime;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

class SmarterRandomPosition extends BaseTrader {

    private final Random random = new Random();

    SmarterRandomPosition(MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        super(clock, instrumentHistoryService);
    }

    @Override
    Optional<Instrument> shouldOpenPosition(MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        LocalDateTime now = clock.now();
        if (!(now.getMinute() == 30)) {
            return Optional.empty();
        }

        Instrument[] instruments = Instrument.values();
        Instrument pair = instruments[random.nextInt(instruments.length)];

        NavigableMap<LocalDateTime, OHLC> oneWeekCandles = instrumentHistoryService.getOHLC(CandleTimeFrame.ONE_DAY, pair, Range.closed(now.minusDays(10), now));

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

            NavigableMap<LocalDateTime, OHLC> dayCandles = instrumentHistoryService.getOHLC(CandleTimeFrame.FOUR_HOURS, pair, Range.closed(now.minusDays(7), now));
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
                return Optional.of(pair);
            } else if (currentHigh < previousHigh && previousHigh < thirdHigh && thirdHigh > fourthHigh && fourthHigh > fifthHigh) {
                return Optional.of(pair.getOpposite());
            }
        }

        return Optional.empty();
    }
}
