package broker;

import java.util.Set;

public class PricingGetRequest {
    private final AccountID accountID;
    private final Set<String> instruments;

    public PricingGetRequest(AccountID accountID, Set<String> instruments) {
        this.accountID = accountID;
        this.instruments = instruments;
    }

    public AccountID getAccountID() {
        return accountID;
    }

    public Set<String> getInstruments() {
        return instruments;
    }
}
