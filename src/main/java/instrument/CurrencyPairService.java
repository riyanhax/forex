package instrument;

import java.time.LocalDateTime;

public interface CurrencyPairService {

    CurrencyPairHistory getData(CurrencyPair pair, LocalDateTime time);

}
