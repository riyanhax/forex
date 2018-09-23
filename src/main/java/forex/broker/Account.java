package forex.broker;

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

import static forex.broker.Quote.formatDollars;
import static forex.broker.Quote.pippetesFromDouble;
import static forex.broker.Quote.profitLossDisplay;

@Entity
public class Account {
    private static final Logger LOG = LoggerFactory.getLogger(Account.class);

    @Id
    private String id;

    @Column(nullable = false)
    private long balance;

    @Column(name = "last_transaction_id", nullable = false)
    private String lastTransactionID;

    @Column(name = "profit_loss", nullable = false)
    private long profitLoss;

    public Account() {
    }

    public Account(String id, long balance, String lastTransactionID, long profitLoss) {
        this.id = id;
        this.balance = balance;
        this.lastTransactionID = lastTransactionID;
        this.profitLoss = profitLoss;
    }

    public String getId() {
        return id;
    }

    public long getBalance() {
        return balance;
    }

    public String getLastTransactionID() {
        return lastTransactionID;
    }

    public long getProfitLoss() {
        return profitLoss;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return balance == account.balance &&
                profitLoss == account.profitLoss &&
                Objects.equals(id, account.id) &&
                Objects.equals(lastTransactionID, account.lastTransactionID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, balance, lastTransactionID, profitLoss);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("balance", formatDollars(balance))
                .add("lastTransactionID", lastTransactionID)
                .add("profitLoss", profitLossDisplay(profitLoss))
                .toString();
    }

    public Account positionOpened(TradeSummary position, String latestTransactionID) {
        long newBalance = this.balance - position.getPurchaseValue();

        return new Account.Builder(this.id)
                .withBalance(newBalance)
                .withLastTransactionID(latestTransactionID)
                .withProfitLoss(this.profitLoss)
                .build();
    }

    public Account positionClosed(TradeSummary position, String latestTransactionID) {
        long newBalance = this.balance + position.getNetAssetValue();
        long newProfitLoss = this.profitLoss + position.getRealizedProfitLoss();

        return new Account.Builder(this.id)
                .withBalance(newBalance)
                .withLastTransactionID(latestTransactionID)
                .withProfitLoss(newProfitLoss)
                .build();
    }

    public static class Builder {
        private final String id;
        private long balance = 0L;
        private String lastTransactionID;
        private long profitLoss = 0L;

        public Builder(String id) {
            this.id = id;
        }

        public Builder withBalanceDollars(int balanceDollars) {
            return withBalance(pippetesFromDouble(balanceDollars));
        }

        public Builder withBalance(long balance) {
            this.balance = balance;
            return this;
        }

        public Builder withLastTransactionID(String lastTransactionID) {
            this.lastTransactionID = lastTransactionID;
            return this;
        }

        public Builder withProfitLoss(long profitLoss) {
            this.profitLoss = profitLoss;
            return this;
        }

        public Account build() {
            Objects.requireNonNull(id);

            return new Account(id, balance, lastTransactionID, profitLoss);
        }

    }
}
