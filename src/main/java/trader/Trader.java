package trader;

import broker.Broker;

import java.time.LocalDateTime;

public interface Trader {

    void advanceTime(LocalDateTime previousTime, LocalDateTime now, Broker broker);

}
