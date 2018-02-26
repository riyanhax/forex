package trader;

import broker.Broker;
import broker.PortfolioValue;
import broker.Position;
import broker.PositionValue;
import market.Instrument;
import market.Market;

public interface Trader<INSTRUMENT extends Instrument, MARKET extends Market<INSTRUMENT>, POSITION extends Position<INSTRUMENT>,
        POSITION_VALUE extends PositionValue<INSTRUMENT, POSITION>, PORTFOLIO_VALUE extends PortfolioValue<INSTRUMENT, POSITION>,
        BROKER extends Broker<INSTRUMENT, MARKET, POSITION, POSITION_VALUE, PORTFOLIO_VALUE>> {

    String getAccountNumber();

    void processUpdates(BROKER broker);

}
