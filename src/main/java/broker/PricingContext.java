package broker;

import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;

public interface PricingContext {

    PricingGetResponse get(PricingGetRequest request) throws RequestException;
}
