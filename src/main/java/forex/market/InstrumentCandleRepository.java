package forex.market;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Set;

public interface InstrumentCandleRepository extends JpaRepository<InstrumentCandle, InstrumentCandleType> {

    @Query("SELECT MAX(ic.id.time) FROM InstrumentCandle ic")
    LocalDateTime findMaxTimestamp();

    Set<InstrumentCandle> findByIdInstrumentAndIdTimeBetweenOrderByIdTime(Instrument instrument, LocalDateTime start, LocalDateTime end);

    @Query("SELECT MAX(ic.midHigh) as high, MIN(ic.midLow) as low FROM InstrumentCandle ic " +
            "WHERE ic.id.time >= ?1 and ic.id.time < ?2")
    HighLowProjection findHighLow(LocalDateTime inclusiveStart, LocalDateTime exclusiveEnd);
}
