package forex.trader;

import forex.broker.AccountAndTrades;
import forex.broker.RequestException;

public interface TraderService {

    AccountAndTrades accountAndTrades(String accountId, int numberClosedTrades) throws RequestException;

}
