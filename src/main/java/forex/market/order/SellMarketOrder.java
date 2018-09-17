package forex.market.order;

public interface SellMarketOrder extends MarketOrder {
    @Override
    default boolean isSellOrder() {
        return true;
    }
}
