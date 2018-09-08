package trader;

import broker.Account;
import broker.Context;
import broker.ForexBroker;
import broker.RequestException;
import broker.TradeSummary;

import java.util.Optional;

public interface ForexTrader {

    String getAccountNumber();

    TradingStrategy getStrategy();

    Optional<TradeSummary> getLastClosedTrade() throws RequestException;

    void processUpdates(ForexBroker broker) throws Exception;

    Optional<Account> getAccount();

    Context getContext();
}
