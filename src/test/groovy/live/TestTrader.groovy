package live

import broker.Context
import market.MarketTime
import trader.TradingStrategies

class TestTrader extends Trader {

    TestTrader(String id, Context context, MarketTime clock) throws Exception {
        super(id, context, TradingStrategies.OPEN_RANDOM_POSITION, clock)
    }

}
