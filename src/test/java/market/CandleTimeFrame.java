package market;

import broker.CandlestickData;

import java.time.DayOfWeek;
import java.time.LocalDate;
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
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            return firstTime.withSecond(0).withNano(0);
        }
    }, FIVE_MINUTE {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusMinutes(5);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            return ONE_MINUTE.calculateStart(firstTime.minusMinutes(firstTime.getMinute() % 5));
        }
    }, FIFTEEN_MINUTE {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusMinutes(15);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            return ONE_MINUTE.calculateStart(firstTime.minusMinutes(firstTime.getMinute() % 15));
        }
    }, THIRTY_MINUTE {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusMinutes(30);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            return ONE_MINUTE.calculateStart(firstTime.minusMinutes(firstTime.getMinute() % 30));
        }
    }, ONE_HOUR {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusHours(1);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            return ONE_MINUTE.calculateStart(firstTime.withMinute(0));
        }
    }, FOUR_HOURS {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusHours(4);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            int hourAdjustment = (endOfTradingHour % 4);
            int hour = firstTime.getHour() - hourAdjustment;

            return ONE_HOUR.calculateStart(firstTime.minusHours(hour % 4));
        }
    }, ONE_DAY {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusDays(1);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            LocalDate candleDay = firstTime.toLocalDate();
            if (firstTime.getHour() < endOfTradingHour) {
                candleDay = candleDay.minusDays(1);
            }
            return candleDay.atTime(endOfTradingHour, 0);
        }
    }, ONE_WEEK {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusWeeks(1);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            return ONE_DAY.calculateStart(firstTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        }
    }, ONE_MONTH {
        @Override
        LocalDateTime nextCandle(LocalDateTime current) {
            return current.plusMonths(1);
        }

        @Override
        LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingHour) {
            return ONE_DAY.calculateStart(firstTime.with(TemporalAdjusters.firstDayOfMonth()));
        }
    };


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

    abstract LocalDateTime nextCandle(LocalDateTime current);

    abstract LocalDateTime calculateStart(LocalDateTime firstTime, int endOfTradingDayHour);

    public final LocalDateTime calculateStart(LocalDateTime time) {
        return calculateStart(time, MarketTime.END_OF_TRADING_DAY_HOUR);
    }
}
