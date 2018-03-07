package broker;

import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;

public interface OrderContext {

    OrderCreateResponse create(OrderCreateRequest request) throws RequestException;

}
