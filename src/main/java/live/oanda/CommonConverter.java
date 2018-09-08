package live.oanda;

import broker.TransactionID;
import com.google.common.base.Preconditions;
import com.oanda.v20.primitives.InstrumentName;
import market.Instrument;
import market.MarketTime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.LocalDateTime.parse;

class CommonConverter {

    static final DateTimeFormatter ISO_INSTANT_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    // Common
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

    static TransactionID convert(com.oanda.v20.transaction.TransactionID oandaVersion) {
        return new TransactionID(oandaVersion.toString());
    }

    static Instrument convert(InstrumentName instrument) {
        return Instrument.bySymbol.get(instrument.toString());
    }
}
