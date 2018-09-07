package live.oanda;

import broker.Account;
import broker.AccountChangesRequest;
import broker.AccountChangesResponse;
import broker.AccountGetResponse;
import broker.AccountID;
import broker.TransactionID;
import com.oanda.v20.account.AccountChanges;

import static broker.Quote.pippetesFromDouble;
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
        return new AccountChangesResponse(lastTransactionID, convert(oandaResponse.getChanges()));
    }

    private static broker.AccountChanges convert(AccountChanges oandaVersion) {
        return new broker.AccountChanges(oandaVersion.getTradesClosed().stream()
                .map(TradeConverter::convert)
                .collect(toList()));
    }

    private static Account convert(com.oanda.v20.account.Account oandaAccount) {
        return new Account(convert(oandaAccount.getId()), CommonConverter.convert(oandaAccount.getLastTransactionID()),
                oandaAccount.getTrades().stream().map(TradeConverter::convert).collect(toList()),
                pippetesFromDouble(oandaAccount.getPl().doubleValue()));
    }

    private static AccountID convert(com.oanda.v20.account.AccountID id) {
        return new AccountID(id.toString());
    }

}
