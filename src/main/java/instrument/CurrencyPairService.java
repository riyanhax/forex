package instrument;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CurrencyPairService {

    Optional<CurrencyPairHistory> getData(CurrencyPair pair, LocalDateTime time);

}
