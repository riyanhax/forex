package forex.broker;

import forex.market.Instrument;

import java.util.Set;

public class PricingGetRequest {
    private final String accountID;
    private final Set<Instrument> instruments;

    public PricingGetRequest(String accountID, Set<Instrument> instruments) {
        this.accountID = accountID;
        this.instruments = instruments;
    }

    public String getAccountID() {
        return accountID;
    }

    public Set<Instrument> getInstruments() {
        return instruments;
    }
}
