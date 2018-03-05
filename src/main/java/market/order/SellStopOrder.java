package market.order;

public interface SellStopOrder extends StopOrder {
    @Override
    default boolean isSellOrder() {
        return true;
    }
}
