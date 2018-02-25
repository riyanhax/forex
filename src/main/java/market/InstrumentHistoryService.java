package market;

import java.time.LocalDateTime;
import java.util.Optional;

public interface InstrumentHistoryService<I extends Instrument, HISTORY extends InstrumentHistory<I>> {

    Optional<HISTORY> getData(I instrument, LocalDateTime time);

}
