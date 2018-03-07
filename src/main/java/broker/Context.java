package broker;

public interface Context {

    PricingContext pricing();

    OrderContext order();

    TradeContext trade();

    AccountContext account();
}
