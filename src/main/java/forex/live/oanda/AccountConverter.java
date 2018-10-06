package forex.live.oanda;

import forex.broker.Account;
import forex.broker.AccountChanges;
import forex.broker.AccountChangesRequest;
import forex.broker.AccountChangesResponse;
import forex.broker.AccountChangesState;
import forex.broker.AccountGetResponse;
import forex.broker.AccountSummary;
import forex.broker.TradeSummary;

import java.util.List;

import static forex.broker.Quote.pippetesFromDouble;
import static java.util.stream.Collectors.toList;

class AccountConverter {

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
        String lastTransactionID = CommonConverter.convert(oandaResponse.getLastTransactionID());

        return new AccountChangesResponse(lastTransactionID, convert(oandaResponse.getChanges(), accountId),
                convert(oandaResponse.getState())
        );
    }

    private static AccountChangesState convert(com.oanda.v20.account.AccountChangesState state) {
        return new AccountChangesState(pippetesFromDouble(state.getNAV().doubleValue()),
                pippetesFromDouble(state.getUnrealizedPL().doubleValue()),
                state.getTrades().stream().map(TradeConverter::convert).collect(toList()));
    }

    private static AccountChanges convert(com.oanda.v20.account.AccountChanges oandaVersion, String accountId) {

        List<String> filledOrders = oandaVersion.getOrdersFilled().stream()
                .map(it -> it.getId().toString())
                .collect(toList());

        List<String> canceledOrders = oandaVersion.getOrdersCancelled().stream()
                .map(it -> it.getId().toString())
                .collect(toList());

        List<TradeSummary> tradesClosed = oandaVersion.getTradesClosed().stream()
                .map(it -> TradeConverter.convert(it, accountId))
                .collect(toList());

        List<TradeSummary> tradesOpened = oandaVersion.getTradesOpened().stream()
                .map(it -> TradeConverter.convert(it, accountId))
                .collect(toList());

        return new AccountChanges(filledOrders, canceledOrders, tradesClosed, tradesOpened);
    }

    private static AccountSummary convert(com.oanda.v20.account.Account oandaAccount) {
        List<TradeSummary> trades = oandaAccount.getTrades().stream().map(it ->
                TradeConverter.convert(it, oandaAccount.getId().toString())).collect(toList());

        return new AccountSummary(new Account.Builder(convert(oandaAccount.getId()))
                .withBalance(pippetesFromDouble(oandaAccount.getBalance().doubleValue()))
                .withLastTransactionID(CommonConverter.convert(oandaAccount.getLastTransactionID()))
                .withProfitLoss(pippetesFromDouble(oandaAccount.getPl().doubleValue()))
                .build(), trades);
    }

    private static String convert(com.oanda.v20.account.AccountID id) {
        return id.toString();
    }

}
