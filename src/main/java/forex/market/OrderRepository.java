package forex.market;

import forex.broker.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository<ORDER extends Order> extends JpaRepository<ORDER, Integer> {

    ORDER findOneByOrderIdAndAccountId(String orderId, String accountID);

}
