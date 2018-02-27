package market.order;

public interface SellLimitOrder extends LimitOrder {
    @Override
    default boolean isSellOrder() {
        return true;
    }
}
