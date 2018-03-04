package live;

import broker.Quote;
import com.google.common.base.MoreObjects;
import com.oanda.v20.pricing.Price;

public class OandaQuote implements Quote {
    private final Price price;

    public OandaQuote(Price price) {
        this.price = price;
    }

    @Override
    public double getBid() {
        return price.getCloseoutBid().doubleValue();
    }

    @Override
    public double getAsk() {
        return price.getCloseoutAsk().doubleValue();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("price", price)
                .toString();
    }
}
