package forex.trader;

import forex.broker.Account;
import forex.broker.Context;
import forex.broker.ForexBroker;
import forex.broker.RequestException;
import forex.broker.TradeSummary;

import java.util.Optional;

public interface ForexTrader {

    String getAccountNumber();

    TradingStrategy getStrategy();

    Optional<TradeSummary> getLastClosedTrade() throws RequestException;

    void processUpdates(ForexBroker broker) throws Exception;

    Optional<Account> getAccount();

    Context getContext();
}
