package forex.broker;

public class TradeSpecifier {
    private final String id;

    public TradeSpecifier(TradeSummary tradeSummary) {
        this(tradeSummary.getTradeId());
    }

    public TradeSpecifier(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
