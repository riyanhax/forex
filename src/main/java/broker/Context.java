package broker;

public interface Context {

    PricingGetResponse getPricing(PricingGetRequest request) throws broker.RequestException;

    AccountChangesResponse accountChanges(AccountChangesRequest request) throws RequestException;

    AccountGetResponse getAccount(AccountID accountID) throws RequestException;

    OrderCreateResponse createOrder(OrderCreateRequest orderCreateRequest) throws RequestException;

    TradeCloseResponse closeTrade(TradeCloseRequest closeRequest) throws RequestException;

    InstrumentCandlesResponse instrumentCandles(InstrumentCandlesRequest request) throws RequestException;

    TradeListResponse listTrade(TradeListRequest request) throws RequestException;
}
