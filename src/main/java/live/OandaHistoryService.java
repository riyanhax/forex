package live;

import com.google.common.collect.Range;
import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.instrument.CandlestickData;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.primitives.DateTime;
import com.oanda.v20.primitives.InstrumentName;
import market.Instrument;
import market.InstrumentHistoryService;
import market.OHLC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.NavigableMap;
import java.util.TreeMap;

import static market.MarketTime.ZONE;

@Service
class OandaHistoryService implements InstrumentHistoryService {

    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final Context ctx;
    private final AccountID accountId;
    private final SystemTime clock;

    OandaHistoryService(SystemTime clock, OandaProperties properties) {
        this.clock = clock;
        this.ctx = new Context(properties.getApi().getEndpoint(), properties.getApi().getToken());
        this.accountId = new AccountID(properties.getApi().getAccount());
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

        InstrumentName instrumentName = new InstrumentName(pair.getBrokerInstrument().getSymbol());
        InstrumentCandlesRequest request = new InstrumentCandlesRequest(instrumentName);
        request.setPrice("M");
        request.setGranularity(granularity);
        request.setAlignmentTimezone(ZONE.getDisplayName(TextStyle.FULL, Locale.US));
        request.setFrom(closed.lowerEndpoint().format(DATE_TIME_FORMATTER));
        request.setTo(closed.upperEndpoint().format(DATE_TIME_FORMATTER));
        request.setIncludeFirst(true);
        request.setDailyAlignment(0);

        InstrumentCandlesResponse response = ctx.instrument.candles(request);

        NavigableMap<LocalDateTime, OHLC> data = new TreeMap<>();

        response.getCandles().forEach(it -> {
            ZonedDateTime zonedDateTime = parseToZone(it.getTime(), ZONE);
            CandlestickData c = it.getMid();

            data.put(zonedDateTime.toLocalDateTime(), new OHLC(c.getO().doubleValue(), c.getH().doubleValue(),
                    c.getL().doubleValue(), c.getC().doubleValue()));
        });
        return data;
    }

    ZonedDateTime parseToZone(DateTime time, ZoneId zone) {
        return ZonedDateTime.parse(time.subSequence(0, 19) + "Z", DATE_TIME_FORMATTER.withZone(zone));
    }
}
