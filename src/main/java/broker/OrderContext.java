package broker;

public interface OrderContext {

    OrderCreateResponse create(OrderCreateRequest request) throws RequestException;

}
