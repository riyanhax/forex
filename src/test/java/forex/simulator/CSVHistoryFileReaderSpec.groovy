package forex.simulator

import forex.broker.CandlestickData
import spock.lang.Specification

import java.time.LocalDateTime

import static forex.market.Instrument.EURUSD
import static java.time.Month.JANUARY

class CSVHistoryFileReaderSpec extends Specification {

    def 'should read history file as UTC-5 minute data and convert to local'() {

        CSVHistoryFileReader fileReader = new CSVHistoryFileReader('/history/Oanda_%s_%d.csv')
        def data = fileReader.instrumentData(EURUSD, 2017)

        def firstSixMinutes = [] as Set
        def iter = data.entrySet().iterator()
        6.times { it -> firstSixMinutes.add(iter.next()) }

        expect:
        firstSixMinutes == [
                (LocalDateTime.of(2017, JANUARY, 2, 17, 0)): new CandlestickData(104684L, 104687L, 104662L, 104680L),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 1)): new CandlestickData(104680L, 104707L, 104675L, 104688L),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 2)): new CandlestickData(104690L, 104711L, 104674L, 104674L),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 3)): new CandlestickData(104670L, 104680L, 104654L, 104680L),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 4)): new CandlestickData(104674L, 104674L, 104646L, 104652L),
                (LocalDateTime.of(2017, JANUARY, 2, 17, 5)): new CandlestickData(104656L, 104688L, 104656L, 104671L)
        ].entrySet()
    }

}