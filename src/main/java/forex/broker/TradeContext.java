package forex.broker;

public interface TradeContext {

    TradeCloseResponse close(TradeCloseRequest request) throws RequestException;

    TradeListResponse list(TradeListRequest request) throws RequestException;
}
