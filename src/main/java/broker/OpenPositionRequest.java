package broker;

import market.Instrument;

import javax.annotation.Nullable;
import java.util.Optional;

public class OpenPositionRequest {
    private final Instrument pair;
    private final Double limit;
    private final Double stopLoss;
    private final Double takeProfit;

    public OpenPositionRequest(Instrument pair, @Nullable Double limit, @Nullable Double stopLoss, @Nullable Double takeProfit) {
        this.pair = pair;
        this.limit = limit;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
    }

    public Instrument getPair() {
        return pair;
    }

    public Optional<Double> getLimit() {
        return Optional.ofNullable(limit);
    }

    public Optional<Double> getStopLoss() {
        return Optional.ofNullable(stopLoss);
    }

    public Optional<Double> getTakeProfit() {
        return Optional.ofNullable(takeProfit);
    }
}
