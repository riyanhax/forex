package broker;

import market.ForexMarket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import trader.Trader;

import java.time.LocalDateTime;
import java.util.List;

@Service
class Oanda implements Broker {

    private static final Logger LOG = LoggerFactory.getLogger(Oanda.class);

    private final ForexMarket forexMarket;
    private final List<Trader> traders;

    public Oanda(ForexMarket forexMarket, List<Trader> traders) {
        this.forexMarket = forexMarket;
        this.traders = traders;
    }

    @Override
    public void advanceTime(LocalDateTime previous, LocalDateTime now) {
        forexMarket.advanceTime(previous, now);

        LOG.info("\tCheck pending orders");
        LOG.info("\tProcess transactions");

        traders.forEach(it -> it.advanceTime(previous, now, this));
    }
}
