package market.order;

public interface OneCancelsOtherOrder {

    Order first();
    Order second();

}
