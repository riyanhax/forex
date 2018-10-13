package forex.live.oanda;

import forex.broker.Account;
import forex.broker.AccountChanges;
import forex.broker.AccountChangesRequest;
import forex.broker.AccountChangesResponse;
import forex.broker.AccountChangesState;
import forex.broker.AccountGetResponse;
import forex.broker.AccountSummary;
import forex.broker.Orders;
import forex.broker.TradeSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static forex.live.oanda.CommonConverter.pippetes;
import static java.util.stream.Collectors.toList;

class AccountConverter {

    private static final Logger LOG = LoggerFactory.getLogger(AccountConverter.class);

    static String id(com.oanda.v20.account.AccountID id) {
        return id == null ? null : id.toString();
    }

    static String id(com.oanda.v20.account.Account account) {
        return account == null ? null : id(account.getId());
    }

    static com.oanda.v20.account.AccountID convert(String accountID) {
        return new com.oanda.v20.account.AccountID(accountID);
    }

    static AccountGetResponse convert(com.oanda.v20.account.AccountGetResponse oandaResponse) {
        return new AccountGetResponse(convert(oandaResponse.getAccount()));
    }

    static com.oanda.v20.account.AccountChangesRequest convert(AccountChangesRequest request) {
        com.oanda.v20.account.AccountID accountID = new com.oanda.v20.account.AccountID(request.getAccountID());

        com.oanda.v20.account.AccountChangesRequest oandaRequest = new com.oanda.v20.account.AccountChangesRequest(accountID);
        oandaRequest.setSinceTransactionID(new com.oanda.v20.transaction.TransactionID(request.getSinceTransactionID()));

        return oandaRequest;

    }

    static AccountChangesResponse convert(com.oanda.v20.account.AccountChangesResponse oandaResponse, String accountId) {
        String lastTransactionID = OrderConverter.id(oandaResponse.getLastTransactionID());

        return new AccountChangesResponse(lastTransactionID, convert(oandaResponse.getChanges(), accountId),
                convert(oandaResponse.getState())
        );
    }

    private static AccountChangesState convert(com.oanda.v20.account.AccountChangesState state) {
        return new AccountChangesState(pippetes(state.getNAV()),
                pippetes(state.getUnrealizedPL()),
                state.getTrades().stream().map(TradeConverter::convert).collect(toList()));
    }

    private static AccountChanges convert(com.oanda.v20.account.AccountChanges oandaVersion, String accountId) {

        Orders createdOrders = OrderConverter.convert(oandaVersion.getOrdersCreated(), accountId);
        Orders filledOrders = OrderConverter.convert(oandaVersion.getOrdersFilled(), accountId);
        Orders canceledOrders = OrderConverter.convert(oandaVersion.getOrdersCancelled(), accountId);

        List<TradeSummary> tradesClosed = oandaVersion.getTradesClosed().stream()
                .map(it -> TradeConverter.convert(it, accountId))
                .collect(toList());

        List<TradeSummary> tradesOpened = oandaVersion.getTradesOpened().stream()
                .map(it -> TradeConverter.convert(it, accountId))
                .collect(toList());

        return new AccountChanges(createdOrders, filledOrders, canceledOrders, tradesClosed, tradesOpened);
    }

    private static AccountSummary convert(com.oanda.v20.account.Account oandaAccount) {
        List<TradeSummary> trades = oandaAccount.getTrades().stream().map(it ->
                TradeConverter.convert(it, id(oandaAccount))).collect(toList());
        Orders pendingOrders = OrderConverter.convert(oandaAccount.getOrders(), id(oandaAccount));

        return new AccountSummary(new Account.Builder(id(oandaAccount.getId()))
                .withBalance(pippetes(oandaAccount.getBalance()))
                .withLastTransactionID(OrderConverter.id(oandaAccount.getLastTransactionID()))
                .withProfitLoss(pippetes(oandaAccount.getPl()))
                .build(), trades, pendingOrders);
    }

}
