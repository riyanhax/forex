package forex.market.order;

public interface BuyMarketOrder extends MarketOrder {
    @Override
    default boolean isBuyOrder() {
        return true;
    }
}
