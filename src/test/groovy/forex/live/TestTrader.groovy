package forex.live

import forex.broker.Context
import forex.market.MarketTime
import forex.trader.Trader
import forex.trader.TraderService
import forex.trader.TradingStrategies

class TestTrader extends Trader {

    TestTrader(String id, Context context, TraderService traderService, MarketTime clock) throws Exception {
        super(id, context, traderService, TradingStrategies.OPEN_RANDOM_POSITION, clock)
    }

}
