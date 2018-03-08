package trader;

import broker.ForexBroker;

public interface ForexTrader {

    String getAccountNumber();

    void processUpdates(ForexBroker broker) throws Exception;
}
