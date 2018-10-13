package forex.live.oanda;

import com.google.common.base.Preconditions;
import com.oanda.v20.pricing.PriceValue;
import com.oanda.v20.primitives.AccountUnits;
import com.oanda.v20.primitives.DateTime;
import com.oanda.v20.primitives.DecimalNumber;
import com.oanda.v20.primitives.InstrumentName;
import forex.market.Instrument;
import forex.market.MarketTime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static forex.broker.Quote.pippetesFromDouble;
import static java.time.LocalDateTime.parse;

class CommonConverter {

    static final DateTimeFormatter ISO_INSTANT_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    static LocalDateTime parseTimestamp(DateTime timestamp) {
        return timestamp == null ? null : parseTimestamp(timestamp.toString());
    }

    static LocalDateTime parseTimestamp(String timestamp) {
        return timestamp == null ? null : parse(timestamp, ISO_INSTANT_FORMATTER.withZone(MarketTime.ZONE));
    }

    static ZonedDateTime parseToZone(String time, ZoneId zone) {
        return ZonedDateTime.parse(time.substring(0, 19) + "Z", ISO_INSTANT_FORMATTER.withZone(zone));
    }

    static void verifyResponseInstrument(Instrument requestedInstrument, Instrument responseInstrument) {
        if (requestedInstrument != responseInstrument) {
            Preconditions.checkArgument(requestedInstrument == responseInstrument.getOpposite(),
                    "Received response instrument %s but requested was %s and not inverse %s",
                    responseInstrument, requestedInstrument, responseInstrument.getOpposite());
        }
    }

    static int toInt(DecimalNumber number) {
        return (int) number.doubleValue();
    }

    static long pippetes(AccountUnits units) {
        return pippetesFromDouble(units.doubleValue());
    }

    static long pippetes(PriceValue price) {
        return pippetesFromDouble(price.doubleValue());
    }

    static long pippetes(boolean inverse, PriceValue price) {
        return pippetesFromDouble(inverse, price.doubleValue());
    }

    static Instrument convert(InstrumentName instrument) {
        return Instrument.bySymbol.get(instrument.toString());
    }
}
