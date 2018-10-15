package forex.broker;

import com.google.common.base.Preconditions;
import forex.market.AccountOrderService;
import forex.market.AccountTransactionService;
import forex.market.Instrument;
import forex.trader.ForexTrader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.Set;

import static forex.broker.OrderService.createLimitOrderRequest;
import static forex.broker.OrderService.createMarketOrderRequest;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final AccountOrderService accountOrderService;
    private final AccountTransactionService accountTransactionService;

    public OrderServiceImpl(AccountOrderService accountOrderService, AccountTransactionService accountTransactionService) {
        this.accountOrderService = accountOrderService;
        this.accountTransactionService = accountTransactionService;
    }

    @Transactional
    @Override
    public void openPosition(ForexTrader trader, OpenPositionRequest request, Quote quote) throws Exception {
        Preconditions.checkArgument(request.getUnits() > 0,
                "Can only request positive units! Open a long position on the inverse for short stances!");

        Instrument pair = request.getPair();

        MarketOrderRequest marketOrderRequest = request.getLimit().isPresent() ? null :
                createMarketOrderRequest(quote, pair, request.getUnits(),
                        request.getStopLoss().orElse(null),
                        request.getTakeProfit().orElse(null));

        LimitOrderRequest limitOrderRequest = request.getLimit().isPresent() ?
                createLimitOrderRequest(quote, pair, request.getUnits(),
                        request.getStopLoss().orElse(null),
                        request.getTakeProfit().orElse(null), request.getLimit().get()) : null;

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(trader.getAccountNumber())
                .setOrder(marketOrderRequest)
                .setOrder(limitOrderRequest);

        OrderCreateResponse orderCreateResponse = trader.getContext().createOrder(orderCreateRequest);
        MarketOrderTransaction orderTransaction = orderCreateResponse.getOrderCreateTransaction();
        LimitOrderTransaction limitOrderTransaction = orderCreateResponse.getLimitOrderCreateTransaction();
        OrderFillTransaction fillTransaction = orderCreateResponse.getOrderFillTransaction();
        OrderCancelTransaction cancelTransaction = orderCreateResponse.getOrderCancelTransaction();
        MarketOrder marketOrder = null;
        LimitOrder limitOrder = null;

        if (orderTransaction != null) {
            marketOrder = new MarketOrder(orderTransaction);

            accountTransactionService.saveIfNotExists(orderTransaction);
        }
        if (limitOrderTransaction != null) {
            limitOrder = new LimitOrder(limitOrderTransaction);

            accountTransactionService.saveIfNotExists(limitOrderTransaction);
        }

        Order order = marketOrder == null ? limitOrder : marketOrder;
        if (order != null) {

            if (fillTransaction != null) {
                order.setFilledTime(fillTransaction.getTime());

                accountTransactionService.saveIfNotExists(fillTransaction);
            } else if (cancelTransaction != null) {
                order.setCanceledTime(cancelTransaction.getTime());
                order.setCanceledReason(cancelTransaction.getReason());

                accountTransactionService.saveIfNotExists(cancelTransaction);
            }

            Set<MarketOrder> marketOrders = marketOrder == null ? emptySet() : singleton(marketOrder);
            Set<LimitOrder> limitOrders = limitOrder == null ? emptySet() : singleton(limitOrder);

            accountOrderService.saveIfNotExists(new Orders(marketOrders, limitOrders, emptySet(), emptySet()));
        }

        LOG.info(orderCreateResponse.toString());
    }
}
