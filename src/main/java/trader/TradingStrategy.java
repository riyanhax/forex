package trader;

import broker.OpenPositionRequest;
import market.InstrumentHistoryService;
import market.MarketTime;

import java.util.Optional;

public interface TradingStrategy {

    Optional<OpenPositionRequest> shouldOpenPosition(MarketTime clock, InstrumentHistoryService instrumentHistoryService) throws Exception;

}
