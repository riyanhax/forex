package market.order;

public interface BuyLimitOrder extends LimitOrder {
    @Override
    default boolean isBuyOrder() {
        return true;
    }
}
