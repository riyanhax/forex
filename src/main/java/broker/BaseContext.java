package broker;

public abstract class BaseContext implements Context {

    @Override
    public PricingGetResponse getPricing(PricingGetRequest request) throws broker.RequestException {
        return pricing().get(request);
    }

    @Override
    public AccountChangesResponse accountChanges(AccountChangesRequest request) throws RequestException {
        return account().changes(request);
    }

    @Override
    public AccountGetResponse getAccount(AccountID accountID) throws RequestException {
        return account().get(accountID);
    }

    @Override
    public OrderCreateResponse createOrder(OrderCreateRequest orderCreateRequest) throws RequestException {
        return order().create(orderCreateRequest);
    }

    @Override
    public TradeCloseResponse closeTrade(TradeCloseRequest closeRequest) throws RequestException {
        return trade().close(closeRequest);
    }

    @Override
    public TradeListResponse listTrade(TradeListRequest request) throws RequestException {
        return trade().list(request);
    }

    @Override
    public InstrumentCandlesResponse instrumentCandles(InstrumentCandlesRequest request) throws RequestException {
        return instrument().candles(request);
    }

    protected abstract PricingContext pricing();

    protected abstract OrderContext order();

    protected abstract TradeContext trade();

    protected abstract AccountContext account();

    protected abstract InstrumentContext instrument();
}
