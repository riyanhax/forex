package live.oanda;

import broker.CandlePrice;
import broker.Candlestick;
import broker.CandlestickData;
import broker.CandlestickGranularity;
import broker.InstrumentCandlesRequest;
import broker.InstrumentCandlesResponse;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.oanda.v20.instrument.WeeklyAlignment;
import com.oanda.v20.primitives.InstrumentName;
import market.Instrument;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static broker.Quote.pippetesFromDouble;
import static java.time.format.TextStyle.NARROW;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static live.oanda.CommonConverter.ISO_INSTANT_FORMATTER;
import static live.oanda.CommonConverter.parseToZone;
import static live.oanda.CommonConverter.verifyResponseInstrument;
import static market.MarketTime.ZONE;

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

        return new Candlestick(timestamp, convert(inverse, data.getAsk()),
                convert(inverse, data.getBid()), convert(inverse, data.getMid()));
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

        double open = data.getO().doubleValue();
        double high = data.getH().doubleValue();
        double low = data.getL().doubleValue();
        double close = data.getC().doubleValue();

        if (inverse) {
            double actualHigh = low;
            low = high;
            high = actualHigh;
        }

        return new CandlestickData(
                pippetesFromDouble(inverse, open),
                pippetesFromDouble(inverse, high),
                pippetesFromDouble(inverse, low),
                pippetesFromDouble(inverse, close));
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
}
