package instrument;

import java.time.ZonedDateTime;

public interface CurrencyPairService {

    CurrencyPairHistory getData(CurrencyPair pair, ZonedDateTime time);

}
