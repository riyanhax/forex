package forex.trader;

import com.google.common.base.MoreObjects;

public class TraderConfiguration {

    private String account;
    private TradingStrategies strategy;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public TradingStrategies getStrategy() {
        return strategy;
    }

    public void setStrategy(TradingStrategies strategy) {
        this.strategy = strategy;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("account", account)
                .add("strategy", strategy)
                .toString();
    }
}
