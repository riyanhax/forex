package trader;

import broker.Broker;

public interface Trader {

    void processUpdates(Broker broker);

}
