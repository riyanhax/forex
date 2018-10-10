package forex.market;

import forex.broker.MarketOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<MarketOrder, Integer> {

    MarketOrder findOneByOrderIdAndAccountId(String orderId, String accountID);
}
