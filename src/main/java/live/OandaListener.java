package live;

import com.oanda.v20.Context;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.pricing.Price;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.transaction.StopLossDetails;
import com.oanda.v20.transaction.TakeProfitDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class OandaListener {

    private static final Logger LOG = LoggerFactory.getLogger(OandaListener.class);
    private static final double STOP_LOSS = 0.0020d;
    private static final double TAKE_PROFIT = 0.0040d;

    private final OandaProperties properties;

    @Autowired
    public OandaListener(OandaProperties properties) {
        this.properties = properties;
    }

    public void run() throws ExecuteException, RequestException {
        Context ctx = new Context(properties.getApi().getEndpoint(), properties.getApi().getToken());
        AccountID accountID = new AccountID(properties.getApi().getAccount());

        try {
            AccountSummary summary = ctx.account.summary(accountID).getAccount();
            LOG.info(summary.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String eurUsd = "EUR_USD";

        List<String> instruments = new ArrayList<>(Arrays.asList(eurUsd, "USD_JPY", "GBP_USD", "USD_CHF"));

        // Poll for prices
        PricingGetRequest request = new PricingGetRequest(accountID, Collections.singletonList(eurUsd));
        PricingGetResponse resp = ctx.pricing.get(request);

        List<Price> prices = resp.getPrices();
        if (prices.isEmpty()) {
            LOG.error("Prices were empty!");
            return;
        }

        for (Price price : prices)
            System.out.println(price);

        Price price = prices.iterator().next();
        double bid = price.getCloseoutBid().doubleValue();

        StopLossDetails stopLoss = new StopLossDetails();
        stopLoss.setPrice(bid - STOP_LOSS);

        TakeProfitDetails takeProfit = new TakeProfitDetails();
        takeProfit.setPrice(bid + TAKE_PROFIT);

        MarketOrderRequest marketOrderRequest = new MarketOrderRequest();
        marketOrderRequest.setInstrument(eurUsd);
        marketOrderRequest.setUnits(1);
        marketOrderRequest.setStopLossOnFill(stopLoss);
        marketOrderRequest.setTakeProfitOnFill(takeProfit);

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(accountID);
        orderCreateRequest.setOrder(marketOrderRequest);

        try {
            OrderCreateResponse orderCreateResponse = ctx.order.create(orderCreateRequest);
            LOG.info(orderCreateResponse.toString());
        } catch (Exception e) {
            LOG.error("Error creating order", e);
        }
    }
}
