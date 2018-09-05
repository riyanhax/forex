package live

import broker.Context
import market.MarketTime
import trader.TradingStrategies

class MockOandaTrader extends OandaTrader {

    MockOandaTrader(Context context, MarketTime clock) throws Exception {
        super(UUID.randomUUID().toString(), context, TradingStrategies.OPEN_RANDOM_POSITION, clock)
    }

}
