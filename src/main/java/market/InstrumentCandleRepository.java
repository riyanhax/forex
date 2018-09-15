package market;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentCandleRepository extends JpaRepository<InstrumentCandle, InstrumentCandleType> {
}
