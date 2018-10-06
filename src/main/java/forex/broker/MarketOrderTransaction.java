package forex.broker;

import com.google.common.base.MoreObjects;
import forex.market.Instrument;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;

@Entity(name = "account_order")
public class MarketOrderTransaction {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "submission_time", nullable = false)
    private LocalDateTime submissionTime;

    @Column(name = "canceled_time")
    private LocalDateTime canceledTime;

    @Column(name = "filled_time")
    private LocalDateTime filledTime;

    @Column(nullable = false)
    private Instrument instrument;

    @Column(nullable = false)
    private int units;

    @Column
    private OrderCancelReason canceledReason;

    public MarketOrderTransaction() {
    }

    public MarketOrderTransaction(String orderId, String accountId, LocalDateTime submissionTime, Instrument instrument, int units) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.submissionTime = submissionTime;
        this.instrument = instrument;
        this.units = units;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public LocalDateTime getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(LocalDateTime submissionTime) {
        this.submissionTime = submissionTime;
    }

    public void setCanceledTime(LocalDateTime canceledTime) {
        this.canceledTime = canceledTime;
    }

    public LocalDateTime getCanceledTime() {
        return canceledTime;
    }

    public void setCanceledReason(OrderCancelReason canceledReason) {
        this.canceledReason = canceledReason;
    }

    public OrderCancelReason getCanceledReason() {
        return canceledReason;
    }

    public void setFilledTime(LocalDateTime filledTime) {
        this.filledTime = filledTime;
    }

    public LocalDateTime getFilledTime() {
        return filledTime;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketOrderTransaction that = (MarketOrderTransaction) o;
        return units == that.units &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(submissionTime, that.submissionTime) &&
                Objects.equals(canceledTime, that.canceledTime) &&
                Objects.equals(canceledReason, that.canceledReason) &&
                Objects.equals(filledTime, that.filledTime) &&
                instrument == that.instrument;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, accountId, submissionTime, canceledTime, canceledReason, filledTime, instrument, units);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("orderId", orderId)
                .add("accountId", accountId)
                .add("submissionTime", submissionTime)
                .add("canceledTime", canceledTime)
                .add("canceledReason", canceledReason)
                .add("filledTime", filledTime)
                .add("instrument", instrument)
                .add("units", units)
                .toString();
    }
}
