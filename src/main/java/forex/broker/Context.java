package forex.broker;

import com.oanda.v20.account.AccountID;

import static forex.broker.TradeStateFilter.CLOSED;

public interface Context {

    int MAXIMUM_CANDLES_PER_RETRIEVAL = 2500;

    PricingGetResponse getPricing(PricingGetRequest request) throws RequestException;

    AccountChangesResponse accountChanges(AccountChangesRequest request) throws RequestException;

    AccountGetResponse getAccount(String accountID) throws RequestException;

    OrderCreateResponse createOrder(OrderCreateRequest orderCreateRequest) throws RequestException;

    TradeCloseResponse closeTrade(TradeCloseRequest closeRequest) throws RequestException;

    InstrumentCandlesResponse instrumentCandles(InstrumentCandlesRequest request) throws RequestException;

    TradeListResponse listTrade(TradeListRequest request) throws RequestException;

    default AccountAndTrades initializeAccount(String accountId, int numLastTrades) throws RequestException {
        Account account = getAccount(accountId).getAccount();
        TradeListResponse tradeListResponse = listTrade(new TradeListRequest(accountId, CLOSED, numLastTrades));

        return new AccountAndTrades(account, tradeListResponse.getTrades());
    }
}
