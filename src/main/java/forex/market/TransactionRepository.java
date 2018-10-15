package forex.market;

import forex.broker.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository<TRANSACTION extends Transaction> extends JpaRepository<TRANSACTION, Integer> {

    TRANSACTION findOneByTransactionIdAndAccountId(String transactionId, String accountID);

}
