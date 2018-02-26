package broker;

import market.Instrument;
import market.Market;
import market.order.OrderRequest;
import simulator.TimeAware;
import trader.Trader;

public interface Broker<INSTRUMENT extends Instrument, MARKET extends Market<INSTRUMENT>, POSITION extends Position<INSTRUMENT>,
        POSITION_VALUE extends PositionValue<INSTRUMENT, POSITION>, PORTFOLIO_VALUE extends PortfolioValue<INSTRUMENT, POSITION>> extends TimeAware {

    PORTFOLIO_VALUE getPortfolio(Trader trader);

    Quote getQuote(INSTRUMENT instrument);

    void orderFilled(OrderRequest<INSTRUMENT> filled);
}
