package live;

import broker.CandlestickData;
import broker.CandlestickGranularity;
import broker.Context;
import broker.InstrumentCandlesRequest;
import broker.InstrumentCandlesResponse;
import broker.RequestException;
import com.google.common.collect.Range;
import market.Instrument;
import market.InstrumentHistoryService;
import market.MarketTime;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NavigableMap;
import java.util.TreeMap;

import static broker.CandlePrice.ASK;
import static broker.CandlePrice.BID;
import static broker.CandlePrice.MID;
import static java.time.DayOfWeek.FRIDAY;
import static java.util.EnumSet.of;
import static market.MarketTime.END_OF_TRADING_DAY_HOUR;

@Service
class OandaHistoryService implements InstrumentHistoryService {
    private final Context ctx;

    OandaHistoryService(OandaProperties properties) {
        this.ctx = OandaContext.create(properties.getApi().getEndpoint(), properties.getApi().getToken());
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getOneDayCandles(Instrument pair, Range<LocalDateTime> closed) throws Exception {
        return getCandles(CandlestickGranularity.D, closed, pair);
    }

    @Override
    public NavigableMap<LocalDateTime, CandlestickData> getFourHourCandles(Instrument pair, Range<LocalDateTime> closed) throws Exception {
        return getCandles(CandlestickGranularity.H4, closed, pair);
    }

    private NavigableMap<LocalDateTime, CandlestickData> getCandles(CandlestickGranularity granularity, Range<LocalDateTime> closed, Instrument pair) throws Exception {

        LocalDateTime exclusiveEnd = closed.upperEndpoint();

        InstrumentCandlesRequest request = new InstrumentCandlesRequest(pair.getBrokerInstrument());
        request.setPrice(of(BID, MID, ASK));
        request.setGranularity(granularity);
        request.setFrom(closed.lowerEndpoint());
        request.setTo(exclusiveEnd);
        request.setIncludeFirst(true);

        if (granularity == CandlestickGranularity.D) {
            request.setAlignmentTimezone(MarketTime.ZONE);
            request.setDailyAlignment(END_OF_TRADING_DAY_HOUR);
        } else if (granularity == CandlestickGranularity.W) {
            request.setWeeklyAlignment(FRIDAY);
        }

        try {
            InstrumentCandlesResponse response = ctx.instrument().candles(request);

            NavigableMap<LocalDateTime, CandlestickData> data = new TreeMap<>();

            response.getCandles().forEach(it -> {
                LocalDateTime timestamp = it.getTime();
                if (timestamp.equals(exclusiveEnd)) { // Force exclusive endpoint behavior
                    return;
                }
                CandlestickData c = it.getMid();

                data.put(timestamp, c);
            });
            return data;
        } catch (RequestException e) {
            throw new Exception(e);
        }
    }
}
