package broker;

import simulator.TimeAware;
import trader.Trader;

public interface Broker extends TimeAware {

    Portfolio getPortfolio(Trader trader);

    Quote getQuote(Instrument instrument);
}
