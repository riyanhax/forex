package forex.market;


import forex.broker.CandlestickData;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public class DatabaseHistoryReader implements OneMinuteCandleReader {

    private final InstrumentCandleRepository instrumentCandleRepo;

    public DatabaseHistoryReader(InstrumentCandleRepository instrumentCandleRepo) {
        this.instrumentCandleRepo = instrumentCandleRepo;
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> instrumentData(Instrument instrument, int year) throws IOException {
        Set<InstrumentCandle> candles = instrumentCandleRepo.findByIdInstrumentAndIdTimeBetweenOrderByIdTime(instrument,
                LocalDateTime.of(year, Month.JANUARY, 1, 0, 0), LocalDateTime.of(year + 1, Month.JANUARY, 1, 0, 0).minusSeconds(1));

        TreeMap<LocalDateTime, CandlestickData> data = new TreeMap<>();

        for (InstrumentCandle candle : candles) {
            data.put(candle.getId().getTime(), new CandlestickData(candle.getMidOpen(), candle.getMidHigh(),
                    candle.getMidLow(), candle.getMidClose()));
        }

        return data;
    }
}
