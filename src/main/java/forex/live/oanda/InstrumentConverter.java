package forex.live.oanda;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.oanda.v20.instrument.WeeklyAlignment;
import com.oanda.v20.primitives.InstrumentName;
import forex.broker.CandlePrice;
import forex.broker.Candlestick;
import forex.broker.CandlestickData;
import forex.broker.CandlestickGranularity;
import forex.broker.InstrumentCandlesRequest;
import forex.broker.InstrumentCandlesResponse;
import forex.market.Instrument;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static forex.live.oanda.CommonConverter.ISO_INSTANT_FORMATTER;
import static forex.live.oanda.CommonConverter.parseToZone;
import static forex.live.oanda.CommonConverter.pippetes;
import static forex.live.oanda.CommonConverter.verifyResponseInstrument;
import static forex.market.MarketTime.ZONE;
import static java.time.format.TextStyle.NARROW;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

class InstrumentConverter {

    static com.oanda.v20.instrument.InstrumentCandlesRequest convert(InstrumentCandlesRequest request) {

        ZonedDateTime start = ZonedDateTime.of(request.getFrom(), ZONE);
        ZonedDateTime end = ZonedDateTime.of(request.getTo(), ZONE);
        String price = request.getPrice().stream().map(CandlePrice::getSymbol).collect(joining(""));

        com.oanda.v20.instrument.InstrumentCandlesRequest oandaRequest = new com.oanda.v20.instrument.InstrumentCandlesRequest(
                new InstrumentName(request.getInstrument().getBrokerInstrument().getSymbol()));
        oandaRequest.setPrice(price);
        oandaRequest.setGranularity(convert(request.getGranularity()));
        // These dates get translated to UTC time via the formatter, which is what Oanda expects
        oandaRequest.setFrom(start.format(ISO_INSTANT_FORMATTER));
        oandaRequest.setTo(end.format(ISO_INSTANT_FORMATTER));
        oandaRequest.setIncludeFirst(request.isIncludeFirst());

        if (request.getAlignmentTimezone() != null) {
            String alignmentTimezone = request.getAlignmentTimezone().getDisplayName(NARROW, Locale.US);

            oandaRequest.setDailyAlignment(request.getDailyAlignment());
            oandaRequest.setAlignmentTimezone(alignmentTimezone);
        }

        if (request.getWeeklyAlignment() != null) {
            oandaRequest.setWeeklyAlignment(convert(request.getWeeklyAlignment()));
        }

        return oandaRequest;
    }

    static InstrumentCandlesResponse convert(Instrument requestedInstrument, com.oanda.v20.instrument.InstrumentCandlesResponse oandaResponse) {
        Instrument responseInstrument = Instrument.bySymbol.get(oandaResponse.getInstrument().toString());
        verifyResponseInstrument(requestedInstrument, responseInstrument);

        return new InstrumentCandlesResponse(
                requestedInstrument,
                convert(oandaResponse.getGranularity()),
                oandaResponse.getCandles().stream().map(it ->
                        convert(requestedInstrument.isInverse(), it)).collect(toList()));
    }

    private static Candlestick convert(boolean inverse, com.oanda.v20.instrument.Candlestick data) {
        ZonedDateTime zonedDateTime = parseToZone(data.getTime().toString(), ZONE);
        LocalDateTime timestamp = zonedDateTime.toLocalDateTime();

        CandlestickData bid = convert(inverse, data.getBid());
        CandlestickData ask = convert(inverse, data.getAsk());
        CandlestickData mid = convert(inverse, data.getMid());

        if (inverse) {
            CandlestickData actualBid = ask;
            ask = bid;
            bid = actualBid;
        }

        Preconditions.checkArgument(bid.getO() < ask.getO() && bid.getH() < ask.getH() && bid.getL() < ask.getL() && bid.getC() < ask.getC(),
                "A bid value seems lesser than the ask value");
        Preconditions.checkArgument(bid.getL() <= bid.getH() && ask.getL() <= ask.getH() && mid.getL() <= mid.getH(),
                "A low value seems greater than the high value");

        return new Candlestick(timestamp, bid, ask, mid);
    }

    private static Map<DayOfWeek, WeeklyAlignment> alignments = stream(DayOfWeek.values())
            .collect(toMap(Function.identity(), it -> WeeklyAlignment.valueOf(it.getDisplayName(TextStyle.FULL, Locale.US))));

    private static WeeklyAlignment convert(DayOfWeek day) {
        return alignments.get(day);
    }

    private static CandlestickData convert(boolean inverse, com.oanda.v20.instrument.CandlestickData data) {
        if (data == null) {
            return null;
        }

        long open = pippetes(inverse, data.getO());
        long high = pippetes(inverse, data.getH());
        long low = pippetes(inverse, data.getL());
        long close = pippetes(inverse, data.getC());

        if (inverse) {
            long actualHigh = low;
            low = high;
            high = actualHigh;
        }

        return new CandlestickData(open, high, low, close);
    }

    private static BiMap<CandlestickGranularity, com.oanda.v20.instrument.CandlestickGranularity> granularities =
            HashBiMap.create(stream(CandlestickGranularity.values())
                    .collect(toMap(Function.identity(), it -> com.oanda.v20.instrument.CandlestickGranularity.valueOf(it.name()))));

    private static com.oanda.v20.instrument.CandlestickGranularity convert(CandlestickGranularity granularity) {
        return granularities.get(granularity);
    }

    private static CandlestickGranularity convert(com.oanda.v20.instrument.CandlestickGranularity oandaVersion) {
        return granularities.inverse().get(oandaVersion);
    }

    static Instrument convert(InstrumentName instrumentName) {
        return Instrument.bySymbol.get(instrumentName.toString());
    }
}
