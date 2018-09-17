package forex.live

import forex.broker.Context
import forex.market.MarketTime
import forex.trader.Trader
import forex.trader.TradingStrategies

class TestTrader extends Trader {

    TestTrader(String id, Context context, MarketTime clock) throws Exception {
        super(id, context, TradingStrategies.OPEN_RANDOM_POSITION, clock)
    }

}
