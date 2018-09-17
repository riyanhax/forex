package forex.live.oanda;

import forex.broker.Account;
import forex.broker.AccountChanges;
import forex.broker.AccountChangesRequest;
import forex.broker.AccountChangesResponse;
import forex.broker.AccountChangesState;
import forex.broker.AccountGetResponse;
import forex.broker.AccountID;
import forex.broker.TradeSummary;
import forex.broker.TransactionID;

import java.util.List;

import static forex.broker.Quote.pippetesFromDouble;
import static java.util.stream.Collectors.toList;

class AccountConverter {

    static com.oanda.v20.account.AccountID convert(AccountID accountID) {
        return new com.oanda.v20.account.AccountID(accountID.getId());
    }

    static AccountGetResponse convert(com.oanda.v20.account.AccountGetResponse oandaResponse) {
        return new AccountGetResponse(convert(oandaResponse.getAccount()));
    }

    static com.oanda.v20.account.AccountChangesRequest convert(AccountChangesRequest request) {
        com.oanda.v20.account.AccountID accountID = new com.oanda.v20.account.AccountID(request.getAccountID().getId());

        com.oanda.v20.account.AccountChangesRequest oandaRequest = new com.oanda.v20.account.AccountChangesRequest(accountID);
        oandaRequest.setSinceTransactionID(new com.oanda.v20.transaction.TransactionID(request.getSinceTransactionID().getId()));

        return oandaRequest;

    }

    static AccountChangesResponse convert(com.oanda.v20.account.AccountChangesResponse oandaResponse) {
        TransactionID lastTransactionID = CommonConverter.convert(oandaResponse.getLastTransactionID());
        return new AccountChangesResponse(lastTransactionID, convert(oandaResponse.getChanges()),
                convert(oandaResponse.getState()));
    }

    private static AccountChangesState convert(com.oanda.v20.account.AccountChangesState state) {
        return new AccountChangesState(pippetesFromDouble(state.getNAV().doubleValue()),
                pippetesFromDouble(state.getUnrealizedPL().doubleValue()),
                state.getTrades().stream().map(TradeConverter::convert).collect(toList()));
    }

    private static AccountChanges convert(com.oanda.v20.account.AccountChanges oandaVersion) {
        List<TradeSummary> tradesClosed = oandaVersion.getTradesClosed().stream()
                .map(TradeConverter::convert)
                .collect(toList());

        List<TradeSummary> tradesOpened = oandaVersion.getTradesOpened().stream()
                .map(TradeConverter::convert)
                .collect(toList());

        return new AccountChanges(tradesClosed, tradesOpened);
    }

    private static Account convert(com.oanda.v20.account.Account oandaAccount) {
        return new Account.Builder(convert(oandaAccount.getId()))
                .withBalance(pippetesFromDouble(oandaAccount.getBalance().doubleValue()))
                .withLastTransactionID(CommonConverter.convert(oandaAccount.getLastTransactionID()))
                .withTrades(oandaAccount.getTrades().stream().map(TradeConverter::convert).collect(toList()))
                .withProfitLoss(pippetesFromDouble(oandaAccount.getPl().doubleValue()))
                .build();
    }

    private static AccountID convert(com.oanda.v20.account.AccountID id) {
        return new AccountID(id.toString());
    }

}
