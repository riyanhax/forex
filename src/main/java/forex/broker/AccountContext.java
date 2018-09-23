package forex.broker;

public interface AccountContext {

    AccountChangesResponse changes(AccountChangesRequest request) throws RequestException;

    AccountGetResponse get(String accountID) throws RequestException;
}
