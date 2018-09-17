package forex.broker;

import com.google.common.base.MoreObjects;
import forex.market.Instrument;

import javax.annotation.Nullable;
import java.util.Optional;

public class OpenPositionRequest {
    private final Instrument pair;
    private final int units;
    private final Long limit;
    private final Long stopLoss;
    private final Long takeProfit;

    public OpenPositionRequest(Instrument pair, int units, @Nullable Long limit, @Nullable Long stopLossPippetes, @Nullable Long takeProfitPippetes) {
        this.pair = pair;
        this.units = units;
        this.limit = limit;
        this.stopLoss = stopLossPippetes;
        this.takeProfit = takeProfitPippetes;
    }

    public Instrument getPair() {
        return pair;
    }

    public int getUnits() {
        return units;
    }

    public Optional<Long> getLimit() {
        return Optional.ofNullable(limit);
    }

    public Optional<Long> getStopLoss() {
        return Optional.ofNullable(stopLoss);
    }

    public Optional<Long> getTakeProfit() {
        return Optional.ofNullable(takeProfit);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("pair", pair)
                .add("limit", limit)
                .add("stopLoss", stopLoss)
                .add("takeProfit", takeProfit)
                .toString();
    }
}
