package market;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

public enum CandleTimeFrame {
    ONE_MINUTE {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusMinutes(1);
        }

        @Override
        public LocalDateTime calculateStart(LocalDateTime firstTime) {
            return firstTime.withSecond(0).withNano(0);
        }
    }, FIVE_MINUTE {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusMinutes(5);
        }

        @Override
        public LocalDateTime calculateStart(LocalDateTime firstTime) {
            return ONE_MINUTE.calculateStart(firstTime.minusMinutes(firstTime.getMinute() % 5));
        }
    }, FIFTEEN_MINUTE {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusMinutes(15);
        }

        @Override
        public LocalDateTime calculateStart(LocalDateTime firstTime) {
            return ONE_MINUTE.calculateStart(firstTime.minusMinutes(firstTime.getMinute() % 15));
        }
    }, THIRTY_MINUTE {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusMinutes(30);
        }

        @Override
        public LocalDateTime calculateStart(LocalDateTime firstTime) {
            return ONE_MINUTE.calculateStart(firstTime.minusMinutes(firstTime.getMinute() % 30));
        }
    }, ONE_HOUR {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusHours(1);
        }

        @Override
        public LocalDateTime calculateStart(LocalDateTime firstTime) {
            return ONE_MINUTE.calculateStart(firstTime.withMinute(0));
        }
    }, FOUR_HOURS {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusHours(4);
        }

        @Override
        public LocalDateTime calculateStart(LocalDateTime firstTime) {
            return ONE_HOUR.calculateStart(firstTime.minusHours(firstTime.getHour() % 4));
        }
    }, ONE_DAY {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusDays(1);
        }

        @Override
        public LocalDateTime calculateStart(LocalDateTime firstTime) {
            return firstTime.toLocalDate().atStartOfDay();
        }
    }, ONE_WEEK {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusWeeks(1);
        }

        @Override
        public LocalDateTime calculateStart(LocalDateTime firstTime) {
            return ONE_DAY.calculateStart(firstTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        }
    }, ONE_MONTH {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusMonths(1);
        }

        @Override
        public LocalDateTime calculateStart(LocalDateTime firstTime) {
            return ONE_DAY.calculateStart(firstTime.with(TemporalAdjusters.firstDayOfMonth()));
        }
    };

    public NavigableMap<LocalDateTime, OHLC> aggregate(NavigableMap<LocalDateTime, OHLC> ohlcData) {

        NavigableMap<LocalDateTime, OHLC> result = new TreeMap<>();
        LocalDateTime firstCandle = calculateStart(ohlcData.firstKey());
        LocalDateTime lastCandle = calculateStart(ohlcData.lastKey());

        for (LocalDateTime current = firstCandle; !current.isAfter(lastCandle); current = nextCandle(current)) {
            SortedMap<LocalDateTime, OHLC> data = ohlcData.subMap(current, nextCandle(current));
            if (data.isEmpty()) {
                continue;
            }
            OHLC candle = OHLC.aggregate(data.values());
            result.put(current, candle);
        }

        return result;
    }

    abstract LocalDateTime nextCandle(LocalDateTime current);

    public abstract LocalDateTime calculateStart(LocalDateTime firstTime);
}
