package live;

import market.InstrumentHistoryService;
import market.MarketTime;
import trader.BaseTrader;
import trader.TradingStrategy;

public class OandaTrader extends BaseTrader {

    public OandaTrader(TradingStrategy tradingStrategy, MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        super(tradingStrategy, clock, instrumentHistoryService);
    }

}
