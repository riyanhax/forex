package broker;

import market.Market;
import simulator.TimeAware;
import trader.Trader;

public interface Broker<I extends Instrument, M extends Market<I>> extends TimeAware {

    PortfolioValue getPortfolio(Trader trader);

    Quote getQuote(I instrument);
}
