package broker;

public class AccountGetResponse {
    private Account account;

    public AccountGetResponse(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }
}
