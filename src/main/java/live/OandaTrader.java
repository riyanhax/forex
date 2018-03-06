package live;

import broker.ForexBroker;
import market.ForexPortfolio;
import market.ForexPortfolioValue;
import market.order.OrderRequest;
import trader.ForexTrader;

public class OandaTrader implements ForexTrader {

    private final ForexTrader tradingStrategy;

    public OandaTrader(ForexTrader tradingStrategy) {
        this.tradingStrategy = tradingStrategy;
    }

    @Override
    public String getAccountNumber() {
        return tradingStrategy.getAccountNumber();
    }

    @Override
    public void processUpdates(ForexBroker broker) throws Exception {
        tradingStrategy.processUpdates(broker);
    }

    @Override
    public void cancelled(OrderRequest cancelled) {
        tradingStrategy.cancelled(cancelled);
    }

    @Override
    public ForexPortfolio getPortfolio() {
        return tradingStrategy.getPortfolio();
    }

    @Override
    public void setPortfolio(ForexPortfolio portfolio) {
        tradingStrategy.setPortfolio(portfolio);
    }
}
