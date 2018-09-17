package forex.broker;

import forex.trader.ForexTrader;

import java.util.List;

/**
 * Container for all configured traders from application.yml
 */
public class LiveTraders {

    private final List<ForexTrader> traders;

    public LiveTraders(List<ForexTrader> traders) {
        this.traders = traders;
    }

    public List<ForexTrader> getTraders() {
        return traders;
    }
}
