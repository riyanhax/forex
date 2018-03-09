package trader;

import broker.ForexBroker;
import broker.OpenPositionRequest;
import market.MarketTime;

import java.util.Optional;

public interface TradingStrategy {

    Optional<OpenPositionRequest> shouldOpenPosition(ForexTrader trader, ForexBroker broker, MarketTime clock) throws Exception;

}
