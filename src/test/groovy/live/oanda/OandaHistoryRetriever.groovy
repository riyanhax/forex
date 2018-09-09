package live.oanda

import broker.Quote
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oanda.v20.instrument.InstrumentCandlesResponse
import com.oanda.v20.order.OrderAdapter
import com.oanda.v20.transaction.TransactionAdapter

import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.function.Function

import static market.Instrument.EURUSD
import static market.MarketTime.ZONE

/**
 * Half-baked utility to retrieve history data from Oanda and write out a CSV file
 * that can be used with the history data service.  Could pull all Oanda history and
 * write a service based on their response JSON.
 */
class OandaHistoryRetriever {

    static int count = 1

    static void main(String[] args) {
        LocalDateTime start = LocalDateTime.of(2017, Month.JANUARY, 1, 1, 00)
        LocalDateTime end = LocalDateTime.of(2017, Month.DECEMBER, 31, 23, 59)

        requestJSON(start, end, new Function<LocalDateTime, LocalDateTime>() {
            @Override
            LocalDateTime apply(LocalDateTime ldt) {
                return ldt.plusDays(3)
            }
        }, "M1")

        for (int i = 1; i < 123; i++) {
            File jsonFile = new File("${i}.json")
            File file = new File(jsonFile.parentFile, "${i}.csv")

            writeCSVFormat(jsonFile.text, file)
        }
    }

    private static void requestJSON(LocalDateTime start, LocalDateTime end,
                                    Function<LocalDateTime, LocalDateTime> nextInterval,
                                    String granularity) {

        for (LocalDateTime ldt = start; !ldt.isAfter(end); ldt = nextInterval.apply(ldt)) {
            def zdt = ZonedDateTime.of(ldt, ZONE);
            def requestEnd = ZonedDateTime.of(nextInterval.apply(ldt).minusMinutes(1), ZONE);

            def from = zdt.format(DateTimeFormatter.ISO_INSTANT)
            def to = requestEnd.format(DateTimeFormatter.ISO_INSTANT)

            def fullUrl = "https://api-fxpractice.oanda.com/v3/instruments/EUR_USD/candles?price=BMA&granularity=${granularity}&from=" +
                    "${from}&to=${to}&includeFirst=true"
            def connection = new URL(fullUrl)
                    .openConnection() as HttpURLConnection

            def token = 'put token here'
            connection.setRequestProperty('Authorization', 'Bearer ' + token)
            connection.setRequestProperty('Accept', 'application/json')

            def json = connection.inputStream.text
            println json

            def outputFile = new File("${count++}.json")
            outputFile.createNewFile()
            outputFile.text = json

            Thread.sleep(1000L);
        }
    }

    private static void writeCSVFormat(String json, File file) {

        file.delete()
        file.createNewFile()

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(com.oanda.v20.order.Order.class, new OrderAdapter())
                .registerTypeAdapter(com.oanda.v20.transaction.Transaction.class, new TransactionAdapter())
                .create();
        def response = InstrumentConverter.convert(EURUSD, gson.fromJson(json, InstrumentCandlesResponse.class))

        file.text = response.getCandles().collect { it ->
            // Convert time to UTC-5
            ZonedDateTime time = ZonedDateTime.of(it.time, ZONE)
            ZonedDateTime utcTime = time.withZoneSameInstant(ZoneId.of("UTC"))
            def ldt = utcTime.toLocalDateTime().minusHours(5)

            return '' + ldt.format(DateTimeFormatter.ofPattern('yyyyMMdd')) + " " + ldt.format(DateTimeFormatter.ofPattern('HHmm')) + "00;" +
                    Quote.doubleFromPippetes(it.mid.o) + ';' + Quote.doubleFromPippetes(it.mid.h) + ';' + Quote.doubleFromPippetes(it.mid.l) +
                    ';' + Quote.doubleFromPippetes(it.mid.c) + ';0'
        }.join('\n')

        println file.text
    }

}
