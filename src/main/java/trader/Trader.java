package trader;

import broker.Broker;

public interface Trader {

    String getAccountNumber();

    void processUpdates(Broker broker);

}
