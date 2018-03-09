package trader;

import broker.ForexBroker;

public interface ForexTrader {

    String getAccountNumber();

    TradingStrategy getStrategy();

    void processUpdates(ForexBroker broker) throws Exception;
}
