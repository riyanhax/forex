package live;

import market.InstrumentHistoryService;
import market.MarketTime;
import trader.BaseTrader;
import trader.TradingStrategy;

public class OandaTrader extends BaseTrader {

    private final String account;

    public OandaTrader(String account, TradingStrategy tradingStrategy, MarketTime clock, InstrumentHistoryService instrumentHistoryService) {
        super(tradingStrategy, clock, instrumentHistoryService);
        this.account = account;
    }

    @Override
    public String getAccountNumber() {
        return account;
    }
}
