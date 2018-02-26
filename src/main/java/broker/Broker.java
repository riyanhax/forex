package broker;

import market.Instrument;
import market.Market;
import market.MarketEngine;
import market.order.OrderRequest;
import simulator.SimulationAware;
import trader.Trader;

public interface Broker<INSTRUMENT extends Instrument, MARKET extends Market<INSTRUMENT>, POSITION extends Position<INSTRUMENT>,
        POSITION_VALUE extends PositionValue<INSTRUMENT, POSITION>, PORTFOLIO_VALUE extends PortfolioValue<INSTRUMENT, POSITION>> extends SimulationAware {

    void processUpdates(MarketEngine<INSTRUMENT> marketEngine);

    PORTFOLIO_VALUE getPortfolio(Trader<INSTRUMENT, MARKET, POSITION, POSITION_VALUE, PORTFOLIO_VALUE, ? extends Broker<INSTRUMENT, MARKET, POSITION, POSITION_VALUE, PORTFOLIO_VALUE>> trader);

    Quote getQuote(INSTRUMENT instrument);

    void orderFilled(OrderRequest<INSTRUMENT> filled);
}
