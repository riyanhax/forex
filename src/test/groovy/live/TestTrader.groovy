package live

import broker.Context
import market.MarketTime
import trader.TradingStrategies

class TestTrader extends Trader {

    TestTrader(Context context, MarketTime clock) throws Exception {
        super(UUID.randomUUID().toString(), context, TradingStrategies.OPEN_RANDOM_POSITION, clock)
    }

}
