package broker;

import com.oanda.v20.account.AccountChangesRequest;
import com.oanda.v20.account.AccountChangesResponse;
import com.oanda.v20.account.AccountGetResponse;
import com.oanda.v20.account.AccountID;

public interface AccountContext {

    AccountChangesResponse changes(AccountChangesRequest request) throws RequestException;

    AccountGetResponse get(AccountID accountID) throws RequestException;
}
