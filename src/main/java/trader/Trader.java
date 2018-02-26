package trader;

import broker.forex.ForexBroker;

public interface Trader {

    String getAccountNumber();

    void processUpdates(ForexBroker broker);

}
