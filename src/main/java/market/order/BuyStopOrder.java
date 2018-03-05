package market.order;

public interface BuyStopOrder extends StopOrder {
    @Override
    default boolean isBuyOrder() {
        return true;
    }
}
