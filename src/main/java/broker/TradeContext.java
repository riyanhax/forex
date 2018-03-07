package broker;

import com.oanda.v20.trade.TradeCloseRequest;
import com.oanda.v20.trade.TradeCloseResponse;

public interface TradeContext {

    TradeCloseResponse close(TradeCloseRequest request) throws RequestException;

}
