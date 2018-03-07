package live;

import broker.Quote;
import com.google.common.base.MoreObjects;
import com.oanda.v20.pricing.Price;

import static broker.Quote.pippetesFromDouble;

public class OandaQuote implements Quote {
    private final Price price;

    public OandaQuote(Price price) {
        this.price = price;
    }

    @Override
    public long getBid() {
        return pippetesFromDouble(price.getCloseoutBid().doubleValue());
    }

    @Override
    public long getAsk() {
        return pippetesFromDouble(price.getCloseoutAsk().doubleValue());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("price", price)
                .toString();
    }
}
