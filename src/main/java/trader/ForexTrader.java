package trader;

import broker.ForexBroker;
import broker.RequestException;
import broker.TradeSummary;

import java.util.Optional;

public interface ForexTrader {

    String getAccountNumber();

    TradingStrategy getStrategy();

    Optional<TradeSummary> getLastClosedTrade() throws RequestException;

    void processUpdates(ForexBroker broker) throws Exception;
}
