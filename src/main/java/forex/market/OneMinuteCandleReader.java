package forex.market;

import forex.broker.CandlestickData;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.NavigableMap;

public interface OneMinuteCandleReader {

    NavigableMap<LocalDateTime, CandlestickData> instrumentData(Instrument instrument, int year) throws IOException;

}
