package broker;

import java.util.List;

public class Account {
    private final AccountID id;
    private final TransactionID lastTransactionID;
    private final List<TradeSummary> trades;
    private double profitLoss;

    public Account(AccountID id, TransactionID lastTransactionID, List<TradeSummary> trades, double profitLoss) {
        this.id = id;
        this.lastTransactionID = lastTransactionID;
        this.trades = trades;
        this.profitLoss = profitLoss;
    }

    public AccountID getId() {
        return id;
    }

    public TransactionID getLastTransactionID() {
        return lastTransactionID;
    }

    public List<TradeSummary> getTrades() {
        return trades;
    }

    public double getPl() {
        return profitLoss;
    }
}
