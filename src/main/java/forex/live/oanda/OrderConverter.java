package forex.live.oanda;

import com.oanda.v20.account.AccountID;
import com.oanda.v20.order.Order;
import com.oanda.v20.order.OrderID;
import com.oanda.v20.order.OrderType;
import com.oanda.v20.transaction.Transaction;
import com.oanda.v20.transaction.TransactionID;
import forex.broker.LimitOrder;
import forex.broker.LimitOrderRequest;
import forex.broker.LimitOrderTransaction;
import forex.broker.MarketOrder;
import forex.broker.MarketOrderRequest;
import forex.broker.MarketOrderTransaction;
import forex.broker.OrderCancelReason;
import forex.broker.OrderCancelTransaction;
import forex.broker.OrderCreateRequest;
import forex.broker.OrderCreateResponse;
import forex.broker.OrderFillTransaction;
import forex.broker.OrderRequest;
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
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static forex.broker.Quote.doubleFromPippetes;
import static forex.broker.Quote.invert;
import static forex.live.oanda.CommonConverter.parseTimestamp;
import static forex.live.oanda.CommonConverter.pippetes;
import static forex.live.oanda.CommonConverter.toInt;
import static forex.live.oanda.CommonConverter.verifyResponseInstrument;
import static java.util.stream.Collectors.groupingBy;

class OrderConverter {

    @FunctionalInterface
    interface InstrumentOrderRequestSetter<O extends com.oanda.v20.order.OrderRequest> {

        void setOrderAttributes(O order, Instrument instrument, int units, com.oanda.v20.transaction.StopLossDetails stopLossDetails, com.oanda.v20.transaction.TakeProfitDetails takeProfitDetails);

    }

    private static final Logger LOG = LoggerFactory.getLogger(OrderConverter.class);

    static String id(TransactionID id) {
        return id.toString();
    }

    static String id(OrderID id) {
        return id.toString();
    }

    static com.oanda.v20.order.OrderCreateRequest convert(OrderCreateRequest request) {
        com.oanda.v20.order.OrderCreateRequest oandaRequest = new com.oanda.v20.order.OrderCreateRequest(
                new AccountID(request.getAccountID()));

        MarketOrderRequest marketOrder = request.getOrder();

        if (marketOrder == null) {
            oandaRequest.setOrder(convert(request.getLimitOrder()));
        } else {
            oandaRequest.setOrder(convert(marketOrder));
        }

        return oandaRequest;
    }

    static OrderCreateResponse convert(Instrument requestedInstrument, com.oanda.v20.order.OrderCreateResponse oandaResponse) {
        MarketOrderTransaction markertOrderCreateTransaction = null;
        LimitOrderTransaction limitOrderCreateTransaction = null;

        Transaction t = oandaResponse.getOrderCreateTransaction();
        if (t instanceof com.oanda.v20.transaction.MarketOrderTransaction) {
            com.oanda.v20.transaction.MarketOrderTransaction marketOrderTransaction = (com.oanda.v20.transaction.MarketOrderTransaction) t;

            markertOrderCreateTransaction = convert(requestedInstrument, marketOrderTransaction);
        } else if (t instanceof com.oanda.v20.transaction.LimitOrderTransaction) {
            com.oanda.v20.transaction.LimitOrderTransaction lt = (com.oanda.v20.transaction.LimitOrderTransaction) t;

            limitOrderCreateTransaction = convert(requestedInstrument, lt);
        } else {
            LOG.error("Unsupported transaction type: {}!", t.getClass().getName());
        }
        OrderFillTransaction fillTransaction = oandaResponse.getOrderFillTransaction() == null ? null :
                convert(oandaResponse.getOrderFillTransaction());
        OrderCancelTransaction cancelTransaction = oandaResponse.getOrderCancelTransaction() == null ? null :
                convert(oandaResponse.getOrderCancelTransaction());

        return new OrderCreateResponse(requestedInstrument, markertOrderCreateTransaction, limitOrderCreateTransaction, fillTransaction, cancelTransaction);
    }

    private static OrderFillTransaction convert(com.oanda.v20.transaction.OrderFillTransaction oandaVersion) {
        return new OrderFillTransaction(oandaVersion.getOrderID().toString(),
                id(oandaVersion.getId()), parseTimestamp(oandaVersion.getTime()));
    }

    private static OrderCancelTransaction convert(com.oanda.v20.transaction.OrderCancelTransaction oandaVersion) {
        return new OrderCancelTransaction(oandaVersion.getOrderID().toString(), OrderCancelReason.valueOf(oandaVersion.getReason().name()),
                id(oandaVersion.getId()), oandaVersion.getRequestID().toString(), parseTimestamp(oandaVersion.getTime())
        );
    }

    static MarketOrderTransaction convert(Instrument requestedInstrument, com.oanda.v20.transaction.MarketOrderTransaction marketOrderTransaction) {
        Instrument responseInstrument = CommonConverter.convert(marketOrderTransaction.getInstrument());
        verifyResponseInstrument(requestedInstrument, responseInstrument);

        int units = toInt(marketOrderTransaction.getUnits());
        if (requestedInstrument != responseInstrument) {
            units *= -1;
        }

        checkArgument(units > 0, "Shouldn't have negative units at this point, why wasn't a short order converted to an inverse long?");

        return new MarketOrderTransaction(marketOrderTransaction.getId().toString(),
                marketOrderTransaction.getAccountID().toString(),
                parseTimestamp(marketOrderTransaction.getTime()),
                requestedInstrument, units);
    }

    // TODO: Consolidate duplication with a MarketOrderTransaction
    static LimitOrderTransaction convert(Instrument requestedInstrument, com.oanda.v20.transaction.LimitOrderTransaction limitOrderTransaction) {
        Instrument responseInstrument = CommonConverter.convert(limitOrderTransaction.getInstrument());
        verifyResponseInstrument(requestedInstrument, responseInstrument);

        int units = toInt(limitOrderTransaction.getUnits());
        long price = pippetes(limitOrderTransaction.getPrice());
        if (requestedInstrument != responseInstrument) {
            units *= -1;
            price = invert(price);
        }

        checkArgument(units > 0, "Shouldn't have negative units at this point, why wasn't a short order converted to an inverse long?");

        return new LimitOrderTransaction(limitOrderTransaction.getId().toString(),
                limitOrderTransaction.getAccountID().toString(),
                parseTimestamp(limitOrderTransaction.getTime()),
                requestedInstrument, units, price);
    }

    private static com.oanda.v20.order.OrderRequest convert(MarketOrderRequest order) {
        return convert(order, new com.oanda.v20.order.MarketOrderRequest(),
                (order1, instrument, units, stopLossDetails, takeProfitDetails) -> {
                    order1.setInstrument(instrument.getSymbol());
                    order1.setUnits(units);
                    order1.setStopLossOnFill(stopLossDetails);
                    order1.setTakeProfitOnFill(takeProfitDetails);
                }, request -> {
                });
    }

    private static com.oanda.v20.order.OrderRequest convert(LimitOrderRequest order) {
        return convert(order, new com.oanda.v20.order.LimitOrderRequest(),
                (order1, instrument, units, stopLossDetails, takeProfitDetails) -> {
                    order1.setInstrument(instrument.getSymbol());
                    order1.setUnits(units);
                    order1.setStopLossOnFill(stopLossDetails);
                    order1.setTakeProfitOnFill(takeProfitDetails);
                }, limitOrderRequest -> limitOrderRequest.setPrice(doubleFromPippetes(order.getInstrument().isInverse() ?
                        invert(order.getPrice()) : order.getPrice()))
        );
    }

    private static <T extends OrderRequest, O extends com.oanda.v20.order.OrderRequest> com.oanda.v20.order.OrderRequest convert(T order,
                                                                                                                                 O oandaOrder,
                                                                                                                                 InstrumentOrderRequestSetter<O> requestSetter,
                                                                                                                                 Consumer<O> orderSpecificsSetter) {
        Instrument instrument = order.getInstrument();
        int units = order.getUnits();

        boolean shorting = instrument.isInverse();
        if (shorting) {
            instrument = instrument.getOpposite();
            units = -units;
        }

        com.oanda.v20.transaction.StopLossDetails stopLossDetails = convert(shorting, order.getStopLossOnFill());
        com.oanda.v20.transaction.TakeProfitDetails takeProfitDetails = convert(shorting, order.getTakeProfitOnFill());

        requestSetter.setOrderAttributes(oandaOrder, instrument, units, stopLossDetails, takeProfitDetails);
        orderSpecificsSetter.accept(oandaOrder);

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

    static Orders convert(List<Order> oandaVersion, String accountId) {

        List<MarketOrder> marketOrders = new ArrayList<>();
        List<LimitOrder> limitOrders = new ArrayList<>();
        List<TakeProfitOrder> takeProfits = new ArrayList<>();
        List<StopLossOrder> stopLosses = new ArrayList<>();

        Map<OrderType, List<Order>> ordersCreated = oandaVersion.stream().collect(groupingBy(Order::getType));
        ordersCreated.forEach((type, orders) -> {
            if (type == OrderType.MARKET) {
                orders.stream()
                        .map(it -> convert((com.oanda.v20.order.MarketOrder) it, accountId))
                        .forEach(marketOrders::add);
            } else if (type == OrderType.LIMIT) {
                orders.stream()
                        .map(it -> convert((com.oanda.v20.order.LimitOrder) it, accountId))
                        .forEach(limitOrders::add);
            } else if (type == OrderType.TAKE_PROFIT) {
                orders.stream()
                        .map(it -> convert((com.oanda.v20.order.TakeProfitOrder) it, accountId))
                        .forEach(takeProfits::add);
            } else if (type == OrderType.STOP_LOSS) {
                orders.stream()
                        .map(it -> convert((com.oanda.v20.order.StopLossOrder) it, accountId))
                        .forEach(stopLosses::add);
            } else {
                LOG.error("Unsupported order type: {}", type);
            }
        });

        return new Orders(marketOrders, limitOrders, takeProfits, stopLosses);
    }

    private static MarketOrder convert(com.oanda.v20.order.MarketOrder oandaVersion, String accountId) {
        LocalDateTime createTime = parseTimestamp(oandaVersion.getCreateTime());
        LocalDateTime canceledTime = parseTimestamp(oandaVersion.getCancelledTime());
        LocalDateTime filledTime = parseTimestamp(oandaVersion.getFilledTime());
        Instrument instrument = CommonConverter.convert(oandaVersion.getInstrument());
        int units = toInt(oandaVersion.getUnits());
        if (units < 0) {
            instrument = instrument.getOpposite();
            units *= -1;
        }

        return new MarketOrder(id(oandaVersion.getId()), accountId, createTime, canceledTime, filledTime, instrument, units);
    }

    private static LimitOrder convert(com.oanda.v20.order.LimitOrder oandaVersion, String accountId) {
        LocalDateTime createTime = parseTimestamp(oandaVersion.getCreateTime());
        LocalDateTime canceledTime = parseTimestamp(oandaVersion.getCancelledTime());
        LocalDateTime filledTime = parseTimestamp(oandaVersion.getFilledTime());
        Instrument instrument = CommonConverter.convert(oandaVersion.getInstrument());
        long price = pippetes(oandaVersion.getPrice());

        int units = toInt(oandaVersion.getUnits());
        if (units < 0) {
            instrument = instrument.getOpposite();
            price = invert(price);
            units *= -1;
        }

        return new LimitOrder(id(oandaVersion.getId()), accountId, createTime, canceledTime, filledTime, instrument, units, price);
    }

    private static TakeProfitOrder convert(com.oanda.v20.order.TakeProfitOrder oandaVersion, String accountId) {
        LocalDateTime createTime = parseTimestamp(oandaVersion.getCreateTime());
        LocalDateTime canceledTime = parseTimestamp(oandaVersion.getCancelledTime());
        LocalDateTime filledTime = parseTimestamp(oandaVersion.getFilledTime());
        long price = pippetes(oandaVersion.getPrice());

        return new TakeProfitOrder(id(oandaVersion.getId()), accountId, createTime, canceledTime, filledTime, price);
    }

    private static StopLossOrder convert(com.oanda.v20.order.StopLossOrder oandaVersion, String accountId) {
        LocalDateTime createTime = parseTimestamp(oandaVersion.getCreateTime());
        LocalDateTime canceledTime = parseTimestamp(oandaVersion.getCancelledTime());
        LocalDateTime filledTime = parseTimestamp(oandaVersion.getFilledTime());
        long price = pippetes(oandaVersion.getPrice());

        return new StopLossOrder(id(oandaVersion.getId()), accountId, createTime, canceledTime, filledTime, price);
    }
}
