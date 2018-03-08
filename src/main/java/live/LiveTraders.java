package live;

import java.util.List;

/**
 * Container for all configured traders from application.yml
 */
public class LiveTraders {

    private final List<OandaTrader> traders;

    public LiveTraders(List<OandaTrader> traders) {
        this.traders = traders;
    }

    public List<OandaTrader> getTraders() {
        return traders;
    }
}
