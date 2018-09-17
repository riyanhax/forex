package forex.simulator;

import forex.broker.Context;
import forex.broker.RequestException;

import java.util.SortedSet;

public interface SimulatorContext extends Context {
    boolean isAvailable();

    void beforeTraders() throws RequestException;

    void afterTraders();

    TraderData getTraderData(String accountNumber);

    SortedSet<TradeHistory> closedTradesForAccountId(String accountNumber);
}
