package forex.broker;

public class TradeCloseRequest {
    private final String accountID;
    private final TradeSpecifier tradeSpecifier;
    private int units;

    public TradeCloseRequest(String accountID, TradeSpecifier tradeSpecifier) {
        this.accountID = accountID;
        this.tradeSpecifier = tradeSpecifier;
    }

    public String getAccountID() {
        return accountID;
    }

    public TradeSpecifier getTradeSpecifier() {
        return tradeSpecifier;
    }

    public void setUnits(int units) {
        this.units = units;
    }
}
