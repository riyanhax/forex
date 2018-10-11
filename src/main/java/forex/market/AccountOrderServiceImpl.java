package forex.market;

import com.google.common.base.Preconditions;
import forex.broker.MarketOrder;
import forex.broker.Order;
import forex.broker.Orders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Transactional
@Service
class AccountOrderServiceImpl implements AccountOrderService {

    public static final Logger LOG = LoggerFactory.getLogger(AccountOrderServiceImpl.class);
    private final MarketOrderRepository marketOrderRepo;

    AccountOrderServiceImpl(MarketOrderRepository marketOrderRepo) {
        this.marketOrderRepo = marketOrderRepo;
    }

    @Override
    public MarketOrder upsert(MarketOrder order) {
        return saveMarketOrder(order, true);
    }

    @Override
    public MarketOrder findMarketOrder(String orderId, String accountID) {
        return marketOrderRepo.findOneByOrderIdAndAccountId(orderId, accountID);
    }

    @Override
    public MarketOrder saveIfNotExists(MarketOrder order) {
        return saveMarketOrder(order, false);
    }

    @Override
    public Orders saveIfNotExists(Orders orders) {
        return persist(orders, false);
    }

    @Override
    public Orders upsert(Orders orders) {
        return persist(orders, true);
    }

    private Orders persist(Orders orders, boolean overwriteExisting) {
        Function<MarketOrder, MarketOrder> marketOrderPersist = overwriteExisting ? this::upsert : this::saveIfNotExists;

        List<MarketOrder> marketOrders = orders.getMarketOrders().stream()
                .map(marketOrderPersist)
                .collect(toList());

        return new Orders(marketOrders, orders.getTakeProfits(), orders.getStopLosses());
    }

    private MarketOrder saveMarketOrder(MarketOrder order, boolean overwriteExisting) {
        return saveOrder(order, overwriteExisting, marketOrderRepo);
    }

    private <ORDER extends Order> ORDER saveOrder(ORDER order,
                                                  boolean overwriteExisting,
                                                  OrderRepository<ORDER> repo) {
        String orderId = order.getOrderId();
        String accountId = order.getAccountId();

        Preconditions.checkNotNull(orderId);
        Preconditions.checkNotNull(accountId);

        ORDER existing = repo.findOneByOrderIdAndAccountId(orderId, accountId);

        if (existing != null) {
            if (overwriteExisting) {
                order.setId(existing.getId());
            } else {
                LOG.warn("Found existing order {}, not overwriting!", existing);

                return existing;
            }
        }

        return repo.save(order);
    }
}
