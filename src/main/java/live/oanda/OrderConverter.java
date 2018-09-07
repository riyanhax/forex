package live.oanda;

import broker.MarketOrderRequest;
import broker.OrderCreateRequest;
import broker.OrderCreateResponse;
import broker.StopLossDetails;
import broker.TakeProfitDetails;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.order.OrderRequest;
import market.Instrument;
import org.slf4j.LoggerFactory;

import static broker.Quote.doubleFromPippetes;
import static broker.Quote.invert;

class OrderConverter {

    static com.oanda.v20.order.OrderCreateRequest convert(OrderCreateRequest request) {
        com.oanda.v20.order.OrderCreateRequest oandaRequest = new com.oanda.v20.order.OrderCreateRequest(
                new AccountID(request.getAccountID().getId()));
        oandaRequest.setOrder(convert(request.getOrder()));

        return oandaRequest;
    }

    static OrderCreateResponse convert(com.oanda.v20.order.OrderCreateResponse oandaResponse) {
        return new OrderCreateResponse();
    }

    private static OrderRequest convert(MarketOrderRequest order) {
        Instrument instrument = order.getInstrument();
        int units = order.getUnits();

        boolean shorting = instrument.isInverse();
        if (shorting) {
            instrument = instrument.getOpposite();
            units = -units;
        }

        com.oanda.v20.transaction.StopLossDetails stopLossDetails = convert(shorting, order.getStopLossOnFill());
        com.oanda.v20.transaction.TakeProfitDetails takeProfitDetails = convert(shorting, order.getTakeProfitOnFill());

        com.oanda.v20.order.MarketOrderRequest oandaOrder = new com.oanda.v20.order.MarketOrderRequest();
        oandaOrder.setInstrument(instrument.getSymbol());
        oandaOrder.setUnits(units);
        oandaOrder.setStopLossOnFill(stopLossDetails);
        oandaOrder.setTakeProfitOnFill(takeProfitDetails);

        LoggerFactory.getLogger(OandaContext.class).info("Converted order {} to {}", order, oandaOrder);

        return oandaOrder;
    }

    private static com.oanda.v20.transaction.StopLossDetails convert(boolean inverse, StopLossDetails stopLossOnFill) {
        long price = stopLossOnFill.getPrice();
        if (inverse) {
            price = invert(price);
        }

        com.oanda.v20.transaction.StopLossDetails oandaVersion = new com.oanda.v20.transaction.StopLossDetails();
        oandaVersion.setPrice(doubleFromPippetes(price));

        return oandaVersion;
    }

    private static com.oanda.v20.transaction.TakeProfitDetails convert(boolean inverse, TakeProfitDetails takeProfit) {
        long price = takeProfit.getPrice();
        if (inverse) {
            price = invert(price);
        }

        com.oanda.v20.transaction.TakeProfitDetails oandaVersion = new com.oanda.v20.transaction.TakeProfitDetails();
        oandaVersion.setPrice(doubleFromPippetes(price));

        return oandaVersion;
    }

}
