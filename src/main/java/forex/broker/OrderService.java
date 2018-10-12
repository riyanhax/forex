package forex.broker;

import forex.market.Instrument;
import forex.trader.ForexTrader;

import javax.annotation.Nullable;

import static forex.broker.Quote.invert;

public interface OrderService {
    void openPosition(ForexTrader trader, OpenPositionRequest request, Quote quote) throws Exception;

    static MarketOrderRequest createMarketOrderRequest(Quote quote, Instrument instrument, int units, @Nullable Long stopLoss, @Nullable Long takeProfit) {
        return populateOrderRequest(new MarketOrderRequest(), quote, instrument, units, stopLoss, takeProfit);
    }

    static LimitOrderRequest createLimitOrderRequest(Quote quote, Instrument instrument, int units, @Nullable Long stopLoss, @Nullable Long takeProfit, long limit) {
        LimitOrderRequest request = populateOrderRequest(new LimitOrderRequest(), quote, instrument, units, stopLoss, takeProfit);
        request.setPrice(limit);

        return request;
    }

    static <T extends OrderRequest> T populateOrderRequest(T request, Quote quote, Instrument instrument, int units, @Nullable Long stopLoss, @Nullable Long takeProfit) {
        // Inverse instruments base stop-losses and take-profits from the ask, since they "buy" when closing the position
        boolean inverse = instrument.isInverse();
        long basePrice = inverse ? quote.getAsk() : quote.getBid();

        request.setInstrument(instrument);
        request.setUnits(units);

        if (stopLoss != null) {
            // Can't seem to get the prices right for inverse positions without converting back and forth
            long price = inverse ? invert(invert(basePrice) + stopLoss)
                    : basePrice - stopLoss;

            StopLossDetails sl = new StopLossDetails();
            sl.setPrice(price);
            request.setStopLossOnFill(sl);
        }

        if (takeProfit != null) {
            // Can't seem to get the prices right for inverse positions without converting back and forth
            long price = inverse ? invert(invert(basePrice) - takeProfit)
                    : basePrice + takeProfit;

            TakeProfitDetails tp = new TakeProfitDetails();
            tp.setPrice(price);
            request.setTakeProfitOnFill(tp);
        }

        return request;
    }
}
