package forex.trader;

import forex.broker.ForexBroker;
import forex.broker.OpenPositionRequest;
import forex.market.MarketTime;

import java.util.Optional;

public interface TradingStrategy {

    Optional<OpenPositionRequest> shouldOpenPosition(ForexTrader trader, ForexBroker broker, MarketTime clock) throws Exception;

    String getName();
}
