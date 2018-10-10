package forex.live.oanda;

import com.oanda.v20.account.AccountID;
import com.oanda.v20.order.Order;
import com.oanda.v20.order.OrderRequest;
import com.oanda.v20.order.OrderType;
import com.oanda.v20.transaction.Transaction;
import forex.broker.MarketOrder;
import forex.broker.MarketOrderRequest;
import forex.broker.MarketOrderTransaction;
import forex.broker.OrderCancelReason;
import forex.broker.OrderCancelTransaction;
import forex.broker.OrderCreateRequest;
import forex.broker.OrderCreateResponse;
import forex.broker.OrderFillTransaction;
import forex.broker.Orders;
import forex.broker.StopLossDetails;
import forex.broker.StopLossOrder;
import forex.broker.TakeProfitDetails;
import forex.broker.TakeProfitOrder;
import forex.market.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static forex.broker.Quote.doubleFromPippetes;
import static forex.broker.Quote.invert;
import static forex.broker.Quote.pippetesFromDouble;
import static forex.live.oanda.CommonConverter.parseTimestamp;
import static forex.live.oanda.CommonConverter.verifyResponseInstrument;
import static java.util.stream.Collectors.groupingBy;

class OrderConverter {

    public static final Logger LOG = LoggerFactory.getLogger(OrderConverter.class);

    static com.oanda.v20.order.OrderCreateRequest convert(OrderCreateRequest request) {
        com.oanda.v20.order.OrderCreateRequest oandaRequest = new com.oanda.v20.order.OrderCreateRequest(
                new AccountID(request.getAccountID()));
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
        OrderFillTransaction fillTransaction = oandaResponse.getOrderFillTransaction() == null ? null :
                convert(oandaResponse.getOrderFillTransaction());
        OrderCancelTransaction cancelTransaction = oandaResponse.getOrderCancelTransaction() == null ? null :
                convert(oandaResponse.getOrderCancelTransaction());

        return new OrderCreateResponse(requestedInstrument, orderCreateTransaction, fillTransaction, cancelTransaction);
    }

    private static OrderFillTransaction convert(com.oanda.v20.transaction.OrderFillTransaction oandaVersion) {
        return new OrderFillTransaction(oandaVersion.getOrderID().toString(),
                oandaVersion.getId().toString(), parseTimestamp(oandaVersion.getTime().toString()));
    }

    private static OrderCancelTransaction convert(com.oanda.v20.transaction.OrderCancelTransaction oandaVersion) {
        return new OrderCancelTransaction(oandaVersion.getOrderID().toString(), OrderCancelReason.valueOf(oandaVersion.getReason().name()),
                oandaVersion.getId().toString(), oandaVersion.getRequestID().toString(), parseTimestamp(oandaVersion.getTime().toString())
        );
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
                marketOrderTransaction.getAccountID().toString(),
                parseTimestamp(marketOrderTransaction.getTime().toString()),
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

    static Orders convert(List<Order> oandaVersion) {

        List<MarketOrder> marketOrders = new ArrayList<>();
        List<TakeProfitOrder> takeProfits = new ArrayList<>();
        List<StopLossOrder> stopLosses = new ArrayList<>();

        Map<OrderType, List<Order>> ordersCreated = oandaVersion.stream().collect(groupingBy(Order::getType));
        ordersCreated.forEach((type, orders) -> {
            if (type == OrderType.MARKET) {
                orders.stream()
                        .map(it -> convert((com.oanda.v20.order.MarketOrder) it))
                        .forEach(marketOrders::add);
            } else if (type == OrderType.TAKE_PROFIT) {
                orders.stream()
                        .map(it -> convert((com.oanda.v20.order.TakeProfitOrder) it))
                        .forEach(takeProfits::add);
            } else if (type == OrderType.STOP_LOSS) {
                orders.stream()
                        .map(it -> convert((com.oanda.v20.order.StopLossOrder) it))
                        .forEach(stopLosses::add);
            } else {
                LOG.error("Unsupported order type: {}", type);
            }
        });

        return new Orders(marketOrders, takeProfits, stopLosses);
    }

    private static MarketOrder convert(com.oanda.v20.order.MarketOrder oandaVersion) {
        LocalDateTime createTime = parseTimestamp(oandaVersion.getCreateTime());
        LocalDateTime canceledTime = parseTimestamp(oandaVersion.getCancelledTime());
        LocalDateTime filledTime = parseTimestamp(oandaVersion.getFilledTime());
        Instrument instrument = CommonConverter.convert(oandaVersion.getInstrument());
        int units = (int) oandaVersion.getUnits().doubleValue();
        if (units < 0) {
            instrument = instrument.getOpposite();
            units *= -1;
        }

        return new MarketOrder(oandaVersion.getId().toString(), createTime, canceledTime, filledTime, instrument, units);
    }

    private static TakeProfitOrder convert(com.oanda.v20.order.TakeProfitOrder oandaVersion) {
        LocalDateTime createTime = parseTimestamp(oandaVersion.getCreateTime());
        LocalDateTime canceledTime = parseTimestamp(oandaVersion.getCancelledTime());
        LocalDateTime filledTime = parseTimestamp(oandaVersion.getFilledTime());
        long price = pippetesFromDouble(oandaVersion.getPrice().doubleValue());

        return new TakeProfitOrder(oandaVersion.getId().toString(), createTime, canceledTime, filledTime, price);
    }

    private static StopLossOrder convert(com.oanda.v20.order.StopLossOrder oandaVersion) {
        LocalDateTime createTime = parseTimestamp(oandaVersion.getCreateTime());
        LocalDateTime canceledTime = parseTimestamp(oandaVersion.getCancelledTime());
        LocalDateTime filledTime = parseTimestamp(oandaVersion.getFilledTime());
        long price = pippetesFromDouble(oandaVersion.getPrice().doubleValue());

        return new StopLossOrder(oandaVersion.getId().toString(), createTime, canceledTime, filledTime, price);
    }
}
