package forex.broker;

import com.google.common.base.Preconditions;
import forex.market.AccountOrderService;
import forex.market.Instrument;
import forex.trader.ForexTrader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static forex.broker.OrderService.createMarketOrderRequest;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class);

    private AccountOrderService accountOrderService;

    public OrderServiceImpl(AccountOrderService accountOrderService) {
        this.accountOrderService = accountOrderService;
    }

    @Transactional
    @Override
    public void openPosition(ForexTrader trader, OpenPositionRequest request, Quote quote) throws Exception {
        Preconditions.checkArgument(request.getUnits() > 0,
                "Can only request positive units! Open a long position on the inverse for short stances!");

        Instrument pair = request.getPair();

        MarketOrderRequest marketOrderRequest = createMarketOrderRequest(quote, pair, request.getUnits(),
                request.getStopLoss().orElse(null),
                request.getTakeProfit().orElse(null));

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(trader.getAccountNumber());
        orderCreateRequest.setOrder(marketOrderRequest);

        OrderCreateResponse orderCreateResponse = trader.getContext().createOrder(orderCreateRequest);
        MarketOrderTransaction orderTransaction = orderCreateResponse.getOrderCreateTransaction();
        OrderFillTransaction fillTransaction = orderCreateResponse.getOrderFillTransaction();
        OrderCancelTransaction cancelTransaction = orderCreateResponse.getOrderCancelTransaction();
        if (orderTransaction != null) {

            MarketOrder order = new MarketOrder(orderTransaction);

            if (fillTransaction != null) {
                order.setFilledTime(fillTransaction.getTime());
            } else if (cancelTransaction != null) {
                order.setCanceledTime(cancelTransaction.getTime());
                order.setCanceledReason(cancelTransaction.getReason());
            }

            accountOrderService.saveIfNotExists(order);
        }

        LOG.info(orderCreateResponse.toString());
    }
}
