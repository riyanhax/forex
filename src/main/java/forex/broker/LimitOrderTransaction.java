package forex.broker;

import com.google.common.base.MoreObjects.ToStringHelper;
import forex.market.Instrument;

import java.time.LocalDateTime;

public class LimitOrderTransaction extends OrderTransaction {

    private final long price;

    public LimitOrderTransaction(String orderId, String accountId, LocalDateTime createTime,
                                 Instrument instrument, int units, long price) {
        super(orderId, accountId, createTime, instrument, units);

        this.price = price;
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("price", price);
    }
}
