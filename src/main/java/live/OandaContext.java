package live;

import broker.AccountContext;
import broker.Context;
import broker.OrderContext;
import broker.PricingContext;
import broker.TradeContext;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountChangesRequest;
import com.oanda.v20.account.AccountChangesResponse;
import com.oanda.v20.account.AccountGetResponse;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.trade.TradeCloseRequest;
import com.oanda.v20.trade.TradeCloseResponse;

public class OandaContext implements Context {

    public static Context create(String endpoint, String token) {
        return new OandaContext(endpoint, token);
    }

    private class OandaPricing implements PricingContext {

        private final com.oanda.v20.pricing.PricingContext pricing;

        OandaPricing(com.oanda.v20.pricing.PricingContext pricing) {
            this.pricing = pricing;
        }

        @Override
        public PricingGetResponse get(PricingGetRequest request) throws broker.RequestException {
            try {
                return pricing.get(request);
            } catch (RequestException e) {
                throw new broker.RequestException(e.getErrorMessage(), e);
            } catch (ExecuteException e) {
                throw new broker.RequestException(e.getMessage(), e);
            }
        }
    }

    private class OandaOrder implements OrderContext {
        private final com.oanda.v20.order.OrderContext order;

        public OandaOrder(com.oanda.v20.order.OrderContext order) {
            this.order = order;
        }

        @Override
        public OrderCreateResponse create(OrderCreateRequest request) throws broker.RequestException {
            try {
                return order.create(request);
            } catch (RequestException e) {
                throw new broker.RequestException(e.getErrorMessage(), e);
            } catch (ExecuteException e) {
                throw new broker.RequestException(e.getMessage(), e);
            }
        }
    }

    private class OandaTrade implements TradeContext {
        private final com.oanda.v20.trade.TradeContext trade;

        public OandaTrade(com.oanda.v20.trade.TradeContext trade) {
            this.trade = trade;
        }

        @Override
        public TradeCloseResponse close(TradeCloseRequest request) throws broker.RequestException {
            try {
                return trade.close(request);
            } catch (RequestException e) {
                throw new broker.RequestException(e.getErrorMessage(), e);
            } catch (ExecuteException e) {
                throw new broker.RequestException(e.getMessage(), e);
            }
        }
    }

    private class OandaAccount implements AccountContext {
        private final com.oanda.v20.account.AccountContext account;

        public OandaAccount(com.oanda.v20.account.AccountContext account) {
            this.account = account;
        }

        @Override
        public AccountChangesResponse changes(AccountChangesRequest request) throws broker.RequestException {
            try {
                return account.changes(request);
            } catch (RequestException e) {
                throw new broker.RequestException(e.getErrorMessage(), e);
            } catch (ExecuteException e) {
                throw new broker.RequestException(e.getMessage(), e);
            }
        }

        @Override
        public AccountGetResponse get(AccountID accountID) throws broker.RequestException {
            try {
                return account.get(accountID);
            } catch (RequestException e) {
                throw new broker.RequestException(e.getErrorMessage(), e);
            } catch (ExecuteException e) {
                throw new broker.RequestException(e.getMessage(), e);
            }
        }
    }

    private final com.oanda.v20.Context ctx;

    private OandaContext(String endpoint, String token) {
        this.ctx = new com.oanda.v20.Context(endpoint, token);
    }

    @Override
    public PricingContext pricing() {
        return new OandaPricing(ctx.pricing);
    }

    @Override
    public OrderContext order() {
        return new OandaOrder(ctx.order);
    }

    @Override
    public TradeContext trade() {
        return new OandaTrade(ctx.trade);
    }

    @Override
    public AccountContext account() {
        return new OandaAccount(ctx.account);
    }
}
