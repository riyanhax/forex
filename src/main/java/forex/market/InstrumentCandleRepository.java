package forex.market;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Set;

public interface InstrumentCandleRepository extends JpaRepository<InstrumentCandle, InstrumentCandleType> {

    @Query("SELECT MAX(ic.id.time) FROM InstrumentCandle ic")
    LocalDateTime findMaxTimestamp();

    Set<InstrumentCandle> findByIdInstrumentAndIdTimeBetweenOrderByIdTime(Instrument instrument, LocalDateTime start, LocalDateTime end);
}
