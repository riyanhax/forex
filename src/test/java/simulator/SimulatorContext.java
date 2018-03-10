package simulator;

import broker.Context;
import broker.RequestException;
import broker.TradeSummary;

import java.util.SortedSet;

public interface SimulatorContext extends Context {
    boolean isAvailable();

    void beforeTraders() throws RequestException;

    void afterTraders();

    TraderData getTraderData(String accountNumber);

    SortedSet<TradeSummary> closedTradesForAccountId(String accountNumber);
}
