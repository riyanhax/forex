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
import market.OHLC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.NavigableMap;
import java.util.TreeMap;

import static broker.Quote.pippetesFromDouble;
import static java.time.DayOfWeek.FRIDAY;
import static market.MarketTime.END_OF_TRADING_DAY_HOUR;
import static market.MarketTime.ZONE;

@Service
class OandaHistoryService implements InstrumentHistoryService {

    private final Context ctx;

    OandaHistoryService(OandaProperties properties) {
        this.ctx = OandaContext.create(properties.getApi().getEndpoint(), properties.getApi().getToken());
    }

    @Override
    public NavigableMap<LocalDateTime, OHLC> getOneDayCandles(Instrument pair, Range<LocalDateTime> closed) throws Exception {
        return getCandles(CandlestickGranularity.D, closed, pair);
    }

    @Override
    public NavigableMap<LocalDateTime, OHLC> getFourHourCandles(Instrument pair, Range<LocalDateTime> closed) throws Exception {
        return getCandles(CandlestickGranularity.H4, closed, pair);
    }

    private NavigableMap<LocalDateTime, OHLC> getCandles(CandlestickGranularity granularity, Range<LocalDateTime> closed, Instrument pair) throws Exception {

        LocalDateTime exclusiveEnd = closed.upperEndpoint();

        ZonedDateTime start = ZonedDateTime.of(closed.lowerEndpoint(), ZONE);
        ZonedDateTime end = ZonedDateTime.of(exclusiveEnd, ZONE);

        InstrumentCandlesRequest request = new InstrumentCandlesRequest(pair.getBrokerInstrument().getSymbol());
        request.setPrice("M");
        request.setGranularity(granularity);
        // These dates get translated to UTC time via the formatter, which is what Oanda expects
        request.setFrom(start.format(DATE_TIME_FORMATTER));
        request.setTo(end.format(DATE_TIME_FORMATTER));
        request.setIncludeFirst(true);

        if (granularity == CandlestickGranularity.D) {
            request.setAlignmentTimezone(MarketTime.ZONE_NAME);
            request.setDailyAlignment(END_OF_TRADING_DAY_HOUR);
        } else if (granularity == CandlestickGranularity.W) {
            request.setWeeklyAlignment(FRIDAY);
        }

        try {
            InstrumentCandlesResponse response = ctx.instrument().candles(request);

            NavigableMap<LocalDateTime, OHLC> data = new TreeMap<>();

            response.getCandles().forEach(it -> {
                ZonedDateTime zonedDateTime = parseToZone(it.getTime(), ZONE);
                LocalDateTime timestamp = zonedDateTime.toLocalDateTime();
                if (timestamp.equals(exclusiveEnd)) { // Force exclusive endpoint behavior
                    return;
                }
                CandlestickData c = it.getMid();

                data.put(timestamp, new OHLC(pippetesFromDouble(c.getO()), pippetesFromDouble(c.getH()),
                        pippetesFromDouble(c.getL()), pippetesFromDouble(c.getC())));
            });
            return data;
        } catch (RequestException e) {
            throw new Exception(e);
        }
    }
}
