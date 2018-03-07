package broker;

public interface TradeContext {

    TradeCloseResponse close(TradeCloseRequest request) throws RequestException;

}
