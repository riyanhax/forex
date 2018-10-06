package forex.market;

import forex.broker.MarketOrderTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<MarketOrderTransaction, Integer> {

    MarketOrderTransaction findOneByOrderIdAndAccountId(String orderId, String accountID);
}
