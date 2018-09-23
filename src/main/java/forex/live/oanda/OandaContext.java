package forex.live.oanda;

import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import forex.broker.AccountChangesRequest;
import forex.broker.AccountChangesResponse;
import forex.broker.AccountContext;
import forex.broker.AccountGetResponse;
import forex.broker.BaseContext;
import forex.broker.Context;
import forex.broker.InstrumentCandlesRequest;
import forex.broker.InstrumentCandlesResponse;
import forex.broker.InstrumentContext;
import forex.broker.OrderContext;
import forex.broker.OrderCreateRequest;
import forex.broker.OrderCreateResponse;
import forex.broker.PricingContext;
import forex.broker.PricingGetRequest;
import forex.broker.PricingGetResponse;
import forex.broker.TradeCloseRequest;
import forex.broker.TradeCloseResponse;
import forex.broker.TradeContext;
import forex.broker.TradeListRequest;
import forex.broker.TradeListResponse;

import java.util.function.Function;

public class OandaContext extends BaseContext {

    @FunctionalInterface
    private interface OandaApi<REQUEST, RESPONSE> {
        RESPONSE send(REQUEST request) throws RequestException, ExecuteException;
    }

    public static Context create(String endpoint, String token) {
        return new OandaContext(endpoint, token);
    }

    private class OandaPricing implements PricingContext {

        private final com.oanda.v20.pricing.PricingContext pricing;

        OandaPricing(com.oanda.v20.pricing.PricingContext pricing) {
            this.pricing = pricing;
        }

        @Override
        public PricingGetResponse get(PricingGetRequest request) throws forex.broker.RequestException {
            return processRequest(request, PricingConverter::convert, pricing::get, it ->
                    PricingConverter.convert(request.getInstruments(), it));
        }
    }

    private class OandaOrder implements OrderContext {
        private final com.oanda.v20.order.OrderContext order;

        OandaOrder(com.oanda.v20.order.OrderContext order) {
            this.order = order;
        }

        @Override
        public OrderCreateResponse create(OrderCreateRequest request) throws forex.broker.RequestException {
            return processRequest(request, OrderConverter::convert, order::create, it ->
                    OrderConverter.convert(request.getOrder().getInstrument(), it));
        }
    }

    private class OandaTrade implements TradeContext {
        private final com.oanda.v20.trade.TradeContext trade;

        OandaTrade(com.oanda.v20.trade.TradeContext trade) {
            this.trade = trade;
        }

        @Override
        public TradeCloseResponse close(TradeCloseRequest request) throws forex.broker.RequestException {
            return processRequest(request, TradeConverter::convert, trade::close, TradeConverter::convert);
        }

        @Override
        public TradeListResponse list(TradeListRequest request) throws forex.broker.RequestException {
            return processRequest(request, TradeConverter::convert, trade::list, TradeConverter::convert);
        }
    }

    private class OandaAccount implements AccountContext {
        private final com.oanda.v20.account.AccountContext account;

        OandaAccount(com.oanda.v20.account.AccountContext account) {
            this.account = account;
        }

        @Override
        public AccountChangesResponse changes(AccountChangesRequest request) throws forex.broker.RequestException {
            return processRequest(request, AccountConverter::convert, account::changes, AccountConverter::convert);
        }

        @Override
        public AccountGetResponse get(String accountID) throws forex.broker.RequestException {
            return processRequest(accountID, AccountConverter::convert, account::get, AccountConverter::convert);
        }
    }

    private class OandaInstrument implements InstrumentContext {
        private final com.oanda.v20.instrument.InstrumentContext instrument;

        OandaInstrument(com.oanda.v20.instrument.InstrumentContext instrument) {
            this.instrument = instrument;
        }

        @Override
        public InstrumentCandlesResponse candles(InstrumentCandlesRequest request) throws forex.broker.RequestException {
            return processRequest(request, InstrumentConverter::convert, instrument::candles, it ->
                    InstrumentConverter.convert(request.getInstrument(), it));
        }
    }

    private final com.oanda.v20.Context ctx;

    private OandaContext(String endpoint, String token) {
        this.ctx = new com.oanda.v20.Context(endpoint, token);
    }

    @Override
    protected PricingContext pricing() {
        return new OandaPricing(ctx.pricing);
    }

    @Override
    protected OrderContext order() {
        return new OandaOrder(ctx.order);
    }

    @Override
    protected TradeContext trade() {
        return new OandaTrade(ctx.trade);
    }

    @Override
    protected AccountContext account() {
        return new OandaAccount(ctx.account);
    }

    @Override
    protected InstrumentContext instrument() {
        return new OandaInstrument(ctx.instrument);
    }

    private <REQUEST, OANDA_REQUEST, RESPONSE, OANDA_RESPONSE> RESPONSE processRequest(REQUEST request,
                                                                                       Function<REQUEST, OANDA_REQUEST> requestConverter,
                                                                                       OandaApi<OANDA_REQUEST, OANDA_RESPONSE> apiCall,
                                                                                       Function<OANDA_RESPONSE, RESPONSE> responseConverter) throws forex.broker.RequestException {
        OANDA_REQUEST oandaRequest = requestConverter.apply(request);

        try {
            OANDA_RESPONSE oandaResponse = apiCall.send(oandaRequest);

            return responseConverter.apply(oandaResponse);
        } catch (RequestException e) {
            throw new forex.broker.RequestException(e.getErrorMessage(), e);
        } catch (ExecuteException e) {
            throw new forex.broker.RequestException(e.getMessage(), e);
        }
    }
}
