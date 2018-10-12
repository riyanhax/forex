package forex.market;

import forex.broker.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface OrderRepository<ORDER extends Order> extends JpaRepository<ORDER, Integer> {

    ORDER findOneByOrderIdAndAccountId(String orderId, String accountID);

    Set<ORDER> findByAccountIdEqualsAndFilledTimeIsNullAndCanceledTimeIsNull(String accountId);

}
