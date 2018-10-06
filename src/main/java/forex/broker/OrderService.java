package forex.broker;

import forex.market.Instrument;
import forex.trader.ForexTrader;

import javax.annotation.Nullable;

import static forex.broker.Quote.invert;

public interface OrderService {
    void openPosition(ForexTrader trader, OpenPositionRequest request, Quote quote) throws Exception;

    static MarketOrderRequest createMarketOrderRequest(Quote quote, Instrument instrument, int units, @Nullable Long stopLoss, @Nullable Long takeProfit) {
        // Inverse instruments base stop-losses and take-profits from the ask, since they "buy" when closing the position
        boolean inverse = instrument.isInverse();
        long basePrice = inverse ? quote.getAsk() : quote.getBid();

        MarketOrderRequest marketOrderRequest = new MarketOrderRequest();
        marketOrderRequest.setInstrument(instrument);
        marketOrderRequest.setUnits(units);

        if (stopLoss != null) {
            // Can't seem to get the prices right for inverse positions without converting back and forth
            long price = inverse ? invert(invert(basePrice) + stopLoss)
                    : basePrice - stopLoss;

            StopLossDetails sl = new StopLossDetails();
            sl.setPrice(price);
            marketOrderRequest.setStopLossOnFill(sl);
        }

        if (takeProfit != null) {
            // Can't seem to get the prices right for inverse positions without converting back and forth
            long price = inverse ? invert(invert(basePrice) - takeProfit)
                    : basePrice + takeProfit;

            TakeProfitDetails tp = new TakeProfitDetails();
            tp.setPrice(price);
            marketOrderRequest.setTakeProfitOnFill(tp);
        }

        return marketOrderRequest;
    }
}
