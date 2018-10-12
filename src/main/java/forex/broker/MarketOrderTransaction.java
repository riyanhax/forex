package forex.broker;

import forex.market.Instrument;

import java.time.LocalDateTime;

public class MarketOrderTransaction extends OrderTransaction {

    public MarketOrderTransaction(String orderId, String accountId, LocalDateTime createTime, Instrument instrument, int units) {
        super(orderId, accountId, createTime, instrument, units);
    }
}
