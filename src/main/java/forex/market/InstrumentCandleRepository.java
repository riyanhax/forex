package forex.market;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Set;

public interface InstrumentCandleRepository extends JpaRepository<InstrumentCandle, InstrumentCandleType> {

    @Query("SELECT MAX(ic.id.time) FROM InstrumentCandle ic")
    LocalDateTime findMaxTimestamp();

    Set<InstrumentCandle> findByIdInstrumentAndIdTimeBetweenOrderByIdTime(Instrument instrument, LocalDateTime start, LocalDateTime end);

    @Query("SELECT MIN(ic.id.time) as open, MAX(ic.midHigh) as high, MIN(ic.midLow) as low, MAX(ic.id.time) as close " +
            "FROM InstrumentCandle ic " +
            "WHERE ic.id.instrument = :instrument AND ic.id.time >= :inclusiveStart and ic.id.time < :exclusiveEnd")
    OhlcProjection findOhlc(@Param("instrument") Instrument instrument,
                            @Param("inclusiveStart") LocalDateTime inclusiveStart,
                            @Param("exclusiveEnd") LocalDateTime exclusiveEnd);
}
