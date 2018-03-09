package broker;

import market.Instrument;

import java.util.Set;

public class PricingGetRequest {
    private final AccountID accountID;
    private final Set<Instrument> instruments;

    public PricingGetRequest(AccountID accountID, Set<Instrument> instruments) {
        this.accountID = accountID;
        this.instruments = instruments;
    }

    public AccountID getAccountID() {
        return accountID;
    }

    public Set<Instrument> getInstruments() {
        return instruments;
    }
}
