package broker;

public class TradeCloseRequest {
    private final AccountID accountID;
    private final TradeSpecifier tradeSpecifier;
    private String units;

    public TradeCloseRequest(AccountID accountID, TradeSpecifier tradeSpecifier) {
        this.accountID = accountID;
        this.tradeSpecifier = tradeSpecifier;
    }

    public AccountID getAccountID() {
        return accountID;
    }

    public TradeSpecifier getTradeSpecifier() {
        return tradeSpecifier;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}
