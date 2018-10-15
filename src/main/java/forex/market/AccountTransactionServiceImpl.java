package forex.market;

import com.google.common.base.Preconditions;
import forex.broker.LimitOrderTransaction;
import forex.broker.MarketOrderTransaction;
import forex.broker.OrderCancelTransaction;
import forex.broker.OrderFillTransaction;
import forex.broker.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Transactional
@Service
class AccountTransactionServiceImpl implements AccountTransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(AccountTransactionServiceImpl.class);

    private final MarketOrderTransactionRepository marketOrderRepo;
    private final LimitOrderTransactionRepository limitOrderRepo;
    private final OrderFillTransactionRepository orderFillRepo;
    private final OrderCancelTransactionRepository orderCancelRepo;

    AccountTransactionServiceImpl(MarketOrderTransactionRepository marketOrderRepo,
                                  LimitOrderTransactionRepository limitOrderRepo,
                                  OrderFillTransactionRepository orderFillRepo,
                                  OrderCancelTransactionRepository orderCancelRepo) {
        this.marketOrderRepo = marketOrderRepo;
        this.limitOrderRepo = limitOrderRepo;
        this.orderFillRepo = orderFillRepo;
        this.orderCancelRepo = orderCancelRepo;
    }

    @Override
    public MarketOrderTransaction saveIfNotExists(MarketOrderTransaction transaction) {
        return save(transaction, false);
    }

    @Override
    public LimitOrderTransaction saveIfNotExists(LimitOrderTransaction transaction) {
        return save(transaction, false);
    }

    @Override
    public OrderFillTransaction saveIfNotExists(OrderFillTransaction transaction) {
        return save(transaction, false);
    }

    @Override
    public OrderCancelTransaction saveIfNotExists(OrderCancelTransaction transaction) {
        return save(transaction, false);
    }

    private MarketOrderTransaction save(MarketOrderTransaction transaction, boolean overwriteExisting) {
        return saveTransaction(transaction, overwriteExisting, marketOrderRepo);
    }

    private LimitOrderTransaction save(LimitOrderTransaction transaction, boolean overwriteExisting) {
        return saveTransaction(transaction, overwriteExisting, limitOrderRepo);
    }

    private OrderFillTransaction save(OrderFillTransaction transaction, boolean overwriteExisting) {
        return saveTransaction(transaction, overwriteExisting, orderFillRepo);
    }

    private OrderCancelTransaction save(OrderCancelTransaction transaction, boolean overwriteExisting) {
        return saveTransaction(transaction, overwriteExisting, orderCancelRepo);
    }

    private <T extends Transaction> T saveTransaction(T order,
                                                boolean overwriteExisting,
                                                TransactionRepository<T> repo) {
        String transactionId = order.getTransactionId();
        String accountId = order.getAccountId();

        Preconditions.checkNotNull(transactionId);
        Preconditions.checkNotNull(accountId);

        T existing = repo.findOneByTransactionIdAndAccountId(transactionId, accountId);

        if (existing != null) {
            if (overwriteExisting) {
                order.setId(existing.getId());
            } else {
                LOG.warn("Found existing transaction {}, not overwriting!", existing);

                return existing;
            }
        }

        return repo.save(order);
    }
}
