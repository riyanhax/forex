package live;

import broker.Price;
import broker.Quote;
import com.google.common.base.MoreObjects;

public class BrokerQuote implements Quote {
    private final Price price;

    BrokerQuote(Price price) {
        this.price = price;
    }

    @Override
    public long getBid() {
        return price.getCloseoutBid();
    }

    @Override
    public long getAsk() {
        return price.getCloseoutAsk();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("price", price)
                .toString();
    }
}
