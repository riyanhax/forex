package forex.broker;

public interface PricingContext {

    PricingGetResponse get(PricingGetRequest request) throws RequestException;
}
