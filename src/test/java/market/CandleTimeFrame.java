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
        public LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusMinutes(1);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.empty();
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            return firstTime.withSecond(0).withNano(0);
        }
    }, FIVE_MINUTE(2) {
        @Override
        public LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusMinutes(5);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.of(ONE_MINUTE);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            return ONE_MINUTE.calculateStart(firstTime.minusMinutes(firstTime.getMinute() % 5));
        }
    }, FIFTEEN_MINUTE(3) {
        @Override
        public LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusMinutes(15);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.of(FIVE_MINUTE);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            return ONE_MINUTE.calculateStart(firstTime.minusMinutes(firstTime.getMinute() % 15));
        }
    }, THIRTY_MINUTE(4) {
        @Override
        public LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusMinutes(30);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.of(FIFTEEN_MINUTE);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            return ONE_MINUTE.calculateStart(firstTime.minusMinutes(firstTime.getMinute() % 30));
        }
    }, ONE_HOUR(5) {
        @Override
        public LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusHours(1);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.of(THIRTY_MINUTE);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            return ONE_MINUTE.calculateStart(firstTime.withMinute(0));
        }
    }, FOUR_HOURS(6) {
        @Override
        public LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusHours(4);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.of(ONE_HOUR);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            int hourAdjustment = (endOfTradingHour % 4);
            int hour = firstTime.getHour() - hourAdjustment;

            return ONE_HOUR.calculateStart(firstTime.minusHours(hour % 4));
        }
    }, ONE_DAY(7) {
        @Override
        public LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusDays(1);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.of(FOUR_HOURS);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            LocalDate candleDay = firstTime.toLocalDate();
            if (firstTime.getHour() < endOfTradingHour) {
                candleDay = candleDay.minusDays(1);
            }
            return candleDay.atTime(endOfTradingHour, 0);
        }
    }, ONE_WEEK(8) {
        @Override
        public LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusWeeks(1);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.of(ONE_DAY);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            return ONE_DAY.calculateStart(firstTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        }
    }, ONE_MONTH(9) {
        @Override
        public LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusMonths(1);
        }

        @Override
        public Optional<CandleTimeFrame> smaller() {
            return Optional.of(ONE_WEEK);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            return ONE_DAY.calculateStart(firstTime.with(TemporalAdjusters.firstDayOfMonth()));
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

    public abstract LocalDateTime nextCandle(LocalDateTime current);

    public abstract Optional<CandleTimeFrame> smaller();

    abstract LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingDayHour);

    public final LocalDateTime calculateStart(LocalDateTime time) {
        return calculateStart(time, MarketTime.END_OF_TRADING_DAY_HOUR);
    }

    public static SortedSet<CandleTimeFrame> descendingSmallerThan(CandleTimeFrame timeFrame) {
        return DESCENDING_TIME.tailSet(timeFrame.smaller().orElseThrow(() ->
                new IllegalArgumentException("None smaller than " + timeFrame)));
    }

}
