package live.oanda;

import broker.MarketOrderRequest;
import broker.MarketOrderTransaction;
import broker.OrderCreateRequest;
import broker.OrderCreateResponse;
import broker.StopLossDetails;
import broker.TakeProfitDetails;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.order.OrderRequest;
import com.oanda.v20.transaction.Transaction;
import market.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static broker.Quote.doubleFromPippetes;
import static broker.Quote.invert;
import static com.google.common.base.Preconditions.checkArgument;
import static live.oanda.CommonConverter.verifyResponseInstrument;

class OrderConverter {

    public static final Logger LOG = LoggerFactory.getLogger(OrderConverter.class);

    static com.oanda.v20.order.OrderCreateRequest convert(OrderCreateRequest request) {
        com.oanda.v20.order.OrderCreateRequest oandaRequest = new com.oanda.v20.order.OrderCreateRequest(
                new AccountID(request.getAccountID().getId()));
        oandaRequest.setOrder(convert(request.getOrder()));

        return oandaRequest;
    }

    static OrderCreateResponse convert(Instrument requestedInstrument, com.oanda.v20.order.OrderCreateResponse oandaResponse) {
        MarketOrderTransaction orderCreateTransaction = null;

        Transaction t = oandaResponse.getOrderCreateTransaction();
        if (t instanceof com.oanda.v20.transaction.MarketOrderTransaction) {
            com.oanda.v20.transaction.MarketOrderTransaction marketOrderTransaction = (com.oanda.v20.transaction.MarketOrderTransaction) t;

            orderCreateTransaction = convert(requestedInstrument, marketOrderTransaction);
        } else {
            LOG.error("Didn't receive a market order create transaction!");
        }

        return new OrderCreateResponse(requestedInstrument, orderCreateTransaction);
    }

    static MarketOrderTransaction convert(Instrument requestedInstrument, com.oanda.v20.transaction.MarketOrderTransaction marketOrderTransaction) {
        Instrument responseInstrument = CommonConverter.convert(marketOrderTransaction.getInstrument());
        verifyResponseInstrument(requestedInstrument, responseInstrument);

        int units = (int) marketOrderTransaction.getUnits().doubleValue();
        if (requestedInstrument != responseInstrument) {
            units *= -1;
        }

        checkArgument(units > 0, "Shouldn't have negative units at this point, why wasn't a short order converted to an inverse long?");

        return new MarketOrderTransaction(marketOrderTransaction.getId().toString(),
                CommonConverter.parseTimestamp(marketOrderTransaction.getTime().toString()),
                requestedInstrument, units);
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
