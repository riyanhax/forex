package market;

import broker.CandlestickData;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public enum CandleTimeFrame {
    ONE_MINUTE(1) {
        @Override
        LocalDateTime adjustToNextCandle(LocalDateTime candleStart) {
            return candleStart.plusMinutes(1);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.empty();
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour, DayOfWeek weeklyAlignment) {
            return firstTime.withSecond(0).withNano(0);
        }
    }, FIVE_MINUTE(2) {
        @Override
        LocalDateTime adjustToNextCandle(LocalDateTime candleStart) {
            return candleStart.plusMinutes(5);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.of(ONE_MINUTE);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour, DayOfWeek weeklyAlignment) {
            return ONE_MINUTE.calculateStart(firstTime.minusMinutes(firstTime.getMinute() % 5));
        }
    }, FIFTEEN_MINUTE(3) {
        @Override
        LocalDateTime adjustToNextCandle(LocalDateTime candleStart) {
            return candleStart.plusMinutes(15);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.of(FIVE_MINUTE);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour, DayOfWeek weeklyAlignment) {
            return ONE_MINUTE.calculateStart(firstTime.minusMinutes(firstTime.getMinute() % 15));
        }
    }, THIRTY_MINUTE(4) {
        @Override
        LocalDateTime adjustToNextCandle(LocalDateTime candleStart) {
            return candleStart.plusMinutes(30);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.of(FIFTEEN_MINUTE);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour, DayOfWeek weeklyAlignment) {
            return ONE_MINUTE.calculateStart(firstTime.minusMinutes(firstTime.getMinute() % 30));
        }
    }, ONE_HOUR(5) {
        @Override
        LocalDateTime adjustToNextCandle(LocalDateTime candleStart) {
            return candleStart.plusHours(1);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.of(THIRTY_MINUTE);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour, DayOfWeek weeklyAlignment) {
            return ONE_MINUTE.calculateStart(firstTime.withMinute(0));
        }
    }, FOUR_HOURS(6) {
        @Override
        LocalDateTime adjustToNextCandle(LocalDateTime candleStart) {
            return candleStart.plusHours(4);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.of(ONE_HOUR);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour, DayOfWeek weeklyAlignment) {
            int hourAdjustment = (endOfTradingHour % 4);
            int hour = firstTime.getHour() - hourAdjustment;

            return ONE_HOUR.calculateStart(firstTime.minusHours(hour % 4));
        }
    }, ONE_DAY(7) {
        @Override
        LocalDateTime adjustToNextCandle(LocalDateTime candleStart) {
            return candleStart.plusDays(1);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.of(FOUR_HOURS);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour, DayOfWeek weeklyAlignment) {
            LocalDate candleDay = firstTime.toLocalDate();
            if (firstTime.getHour() < endOfTradingHour) {
                candleDay = candleDay.minusDays(1);
            }
            return candleDay.atTime(endOfTradingHour, 0);
        }
    }, ONE_WEEK(8) {
        @Override
        LocalDateTime adjustToNextCandle(LocalDateTime candleStart) {
            return candleStart.plusWeeks(1);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.of(ONE_DAY);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour, DayOfWeek weeklyAlignment) {
            return ONE_DAY.calculateStart(firstTime).with(TemporalAdjusters.previousOrSame(weeklyAlignment));
        }
    }, ONE_MONTH(9) {
        @Override
        LocalDateTime adjustToNextCandle(LocalDateTime candleStart) {
            for (int i = 1; i < 3; i++) {
                LocalDateTime startOfNextTradingMonth = calculateStart(candleStart.plusMonths(i));

                if (!startOfNextTradingMonth.equals(candleStart)) {
                    return startOfNextTradingMonth;
                }
            }
            throw new IllegalStateException("Was unable to calculate next trading month start in 2 iterations!");
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.of(ONE_WEEK);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour, DayOfWeek weeklyAlignment) {
            // Have to make sure the date time is not at the very END of the calendar month but also
            // the START of the new trading month.  For instance, January 31, 1700 Eastern starts the first trading day
            // of the February trading month
            LocalDateTime fullyIntoTradingDay = firstTime.getHour() < endOfTradingHour ? firstTime : firstTime.plusDays(1);
            LocalDateTime startOfMonth = fullyIntoTradingDay.with(TemporalAdjusters.firstDayOfMonth())
                    .withHour(0); // Just some time into the first trading day, not important which specific hour

            return ONE_DAY.calculateStart(startOfMonth);
        }
    };

    private final int sortOrder;

    CandleTimeFrame(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    private static final SortedSet<CandleTimeFrame> DESCENDING_TIME =
            Arrays.stream(CandleTimeFrame.values()).collect(Collectors.toCollection((() ->
                    new TreeSet<>(comparing(CandleTimeFrame::getSortOrder).reversed()))));

    public NavigableMap<LocalDateTime, CandlestickData> aggregate(NavigableMap<LocalDateTime, CandlestickData> ohlcData) {

        NavigableMap<LocalDateTime, CandlestickData> result = new TreeMap<>();
        LocalDateTime firstCandle = calculateStart(ohlcData.firstKey());
        LocalDateTime lastCandle = calculateStart(ohlcData.lastKey());

        for (LocalDateTime current = firstCandle; !current.isAfter(lastCandle); current = nextCandle(current)) {
            SortedMap<LocalDateTime, CandlestickData> data = ohlcData.subMap(current, nextCandle(current));
            if (data.isEmpty()) {
                continue;
            }
            CandlestickData candle = CandlestickData.aggregate(data.values());
            result.put(current, candle);
        }

        return result;
    }

    public abstract Optional<CandleTimeFrame> smaller();

    abstract LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingDayHour, DayOfWeek weeklyAlignment);

    abstract LocalDateTime adjustToNextCandle(LocalDateTime candleStart);

    public final LocalDateTime calculateStart(LocalDateTime time) {
        return calculateStart(time, MarketTime.END_OF_TRADING_DAY_HOUR, MarketTime.WEEKLY_ALIGNMENT);
    }

    public final LocalDateTime nextCandle(LocalDateTime current) {
        return adjustToNextCandle(calculateStart(current));
    }

    public static SortedSet<CandleTimeFrame> descendingSmallerThan(CandleTimeFrame timeFrame) {
        return DESCENDING_TIME.tailSet(timeFrame.smaller().orElseThrow(() ->
                new IllegalArgumentException("None smaller than " + timeFrame)));
    }

}
